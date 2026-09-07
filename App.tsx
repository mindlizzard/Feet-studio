import React, { useEffect, useMemo, useRef, useState } from "react";
import ControlPanel from "./components/ControlPanel";
import GalleryStrip from "./components/GalleryStrip";
import PromptInspector from "./components/PromptInspector";
import RenderBar from "./components/RenderBar";
import StudioNavigator, { MOBILE_NAV_ITEMS } from "./components/StudioNavigator";
import PoseVisualizer from "./components/PoseVisualizer";
import { DesignState, INITIAL_STATE } from "./types";
import {
  FixTarget,
  generateImage,
  generateSocialCaption,
  generateTargetedFix,
  inspectRender,
} from "./services/geminiService";
import { buildRenderContract, getRenderReadiness } from "./services/renderPipeline";
import { listRenderRecords, saveRenderRecord } from "./services/studioDb";
import {
  INITIAL_STUDIO_V4,
  ReferenceAsset,
  RenderRecord,
  StudioSection,
  StudioV4State,
  WorkspaceState,
} from "./typesV4";

const STORAGE_KEY = "feetstudio.v4.workspace";

const loadWorkspace = (): WorkspaceState => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return { design: INITIAL_STATE, studio: INITIAL_STUDIO_V4 };
    const parsed = JSON.parse(raw);
    return {
      design: { ...INITIAL_STATE, ...(parsed.design || {}) },
      studio: {
        ...INITIAL_STUDIO_V4,
        ...(parsed.studio || {}),
        foot: { ...INITIAL_STUDIO_V4.foot, ...(parsed.studio?.foot || {}) },
        skin: { ...INITIAL_STUDIO_V4.skin, ...(parsed.studio?.skin || {}) },
        hosiery: { ...INITIAL_STUDIO_V4.hosiery, ...(parsed.studio?.hosiery || {}) },
        camera: { ...INITIAL_STUDIO_V4.camera, ...(parsed.studio?.camera || {}) },
        lighting: { ...INITIAL_STUDIO_V4.lighting, ...(parsed.studio?.lighting || {}) },
        locks: { ...INITIAL_STUDIO_V4.locks, ...(parsed.studio?.locks || {}) },
      },
    };
  } catch {
    return { design: INITIAL_STATE, studio: INITIAL_STUDIO_V4 };
  }
};

const App: React.FC = () => {
  const [workspace, setWorkspace] = useState<WorkspaceState>(loadWorkspace);
  const [section, setSection] = useState<StudioSection>("feet");
  const [references, setReferences] = useState<ReferenceAsset[]>([]);
  const [gallery, setGallery] = useState<RenderRecord[]>([]);
  const [activeRecord, setActiveRecord] = useState<RenderRecord | null>(null);
  const [previewMode, setPreviewMode] = useState<"image" | "pose">("image");
  const [mobilePanelOpen, setMobilePanelOpen] = useState(false);
  const [promptOpen, setPromptOpen] = useState(false);
  const [fixMenuOpen, setFixMenuOpen] = useState(false);
  const [isGenerating, setIsGenerating] = useState(false);
  const [progress, setProgress] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<string | null>(null);
  const [caption, setCaption] = useState("");
  const undoStack = useRef<WorkspaceState[]>([]);
  const redoStack = useRef<WorkspaceState[]>([]);
  const stopAfterCurrent = useRef(false);

  const contractPreview = useMemo(
    () => buildRenderContract(workspace.design, workspace.studio, references).contract,
    [workspace, references]
  );
  const readiness = useMemo(() => getRenderReadiness(contractPreview), [contractPreview]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(workspace));
  }, [workspace]);

  useEffect(() => {
    listRenderRecords()
      .then((records) => {
        setGallery(records);
        if (records[0]) setActiveRecord(records[0]);
      })
      .catch((err) => console.warn("Gallery load failed", err));
  }, []);

  useEffect(() => {
    if (!toast) return;
    const timer = window.setTimeout(() => setToast(null), 2600);
    return () => window.clearTimeout(timer);
  }, [toast]);

  const commit = (next: WorkspaceState) => {
    undoStack.current.push(JSON.parse(JSON.stringify(workspace)));
    if (undoStack.current.length > 80) undoStack.current.shift();
    redoStack.current = [];
    setWorkspace(next);
  };

  const onChange = (updates: Partial<DesignState>) =>
    commit({ ...workspace, design: { ...workspace.design, ...updates } });

  const onStudioChange = (updates: Partial<StudioV4State>) =>
    commit({ ...workspace, studio: { ...workspace.studio, ...updates } });

  const undo = () => {
    const previous = undoStack.current.pop();
    if (!previous) return;
    redoStack.current.push(JSON.parse(JSON.stringify(workspace)));
    setWorkspace(previous);
  };

  const redo = () => {
    const next = redoStack.current.pop();
    if (!next) return;
    undoStack.current.push(JSON.parse(JSON.stringify(workspace)));
    setWorkspace(next);
  };

  const persistRecord = async (record: RenderRecord) => {
    await saveRenderRecord(record);
    setGallery((prev) => [record, ...prev.filter((item) => item.id !== record.id)].slice(0, 60));
    setActiveRecord(record);
  };

  const runInspector = async (record: RenderRecord): Promise<RenderRecord> => {
    if (workspace.studio.inspectorMode === "off") return record;
    setProgress("Inspecting anatomy & materials...");
    try {
      const inspector = await inspectRender(record.imageData, record, workspace.studio.inspectorMode);
      return { ...record, inspector };
    } catch (err) {
      console.warn("Inspector failed", err);
      return record;
    }
  };

  const handleGenerate = async () => {
    setError(null);
    setIsGenerating(true);
    setFixMenuOpen(false);
    stopAfterCurrent.current = false;

    try {
      for (let index = 0; index < workspace.studio.batchCount; index += 1) {
        if (stopAfterCurrent.current) break;
        setProgress(`Generating ${index + 1}/${workspace.studio.batchCount}...`);
        const result = await generateImage(workspace.design, workspace.studio, references);
        let record: RenderRecord = {
          id: result.contract.id,
          createdAt: new Date().toISOString(),
          imageData: result.imageData,
          favorite: false,
          contract: result.contract,
        };
        setActiveRecord(record);
        setPreviewMode("image");
        record = await runInspector(record);
        await persistRecord(record);

        if (index === 0) {
          generateSocialCaption(workspace.design)
            .then(setCaption)
            .catch((err) => console.warn("Caption failed", err));
        }
      }
      setToast(stopAfterCurrent.current ? "Batch gestopt na huidige render." : "Render opgeslagen in Gallery ✓");
    } catch (err: any) {
      setError(err?.message || "Generatie mislukt.");
    } finally {
      setProgress("");
      setIsGenerating(false);
      stopAfterCurrent.current = false;
    }
  };

  const handleFix = async (target: FixTarget) => {
    if (!activeRecord) return;
    setIsGenerating(true);
    setError(null);
    setFixMenuOpen(false);
    setProgress(`Targeted Fix: ${target}...`);
    try {
      const result = await generateTargetedFix(activeRecord, target, workspace.studio, references);
      let record: RenderRecord = {
        id: result.contract.id,
        createdAt: new Date().toISOString(),
        imageData: result.imageData,
        parentRenderId: activeRecord.id,
        fixTarget: target,
        favorite: false,
        contract: result.contract,
      };
      record = await runInspector(record);
      await persistRecord(record);
      setToast(`${target} fix opgeslagen ✓`);
    } catch (err: any) {
      setError(err?.message || "Targeted Fix mislukt.");
    } finally {
      setProgress("");
      setIsGenerating(false);
    }
  };

  const handleDownload = () => {
    if (!activeRecord) return;
    const link = document.createElement("a");
    link.href = activeRecord.imageData;
    link.download = `feet-studio-v4-${Date.now()}.png`;
    link.click();
  };

  const handleShare = async () => {
    if (!activeRecord) return;
    if (caption && navigator.clipboard) {
      try {
        await navigator.clipboard.writeText(caption);
        setToast("Caption gekopieerd.");
      } catch {}
    }
    if (navigator.share) {
      try {
        const blob = await (await fetch(activeRecord.imageData)).blob();
        const file = new File([blob], "feet-studio-v4.png", { type: blob.type || "image/png" });
        await navigator.share({ title: "Feet Studio v4", text: caption, files: [file] });
      } catch {}
    }
  };

  const openSection = (next: StudioSection) => {
    setSection(next);
    if (window.innerWidth < 768) setMobilePanelOpen(true);
  };

  return (
    <div className="studio-app">
      {toast && <div className="studio-toast">{toast}</div>}

      <header className="studio-topbar">
        <div className="flex min-w-0 items-center gap-3">
          <div className="text-[15px] font-black tracking-tight text-white">
            Velvet<span className="text-fuchsia-400">Sole</span>
            <span className="ml-2 rounded-md bg-white/5 px-1.5 py-0.5 text-[9px] font-bold text-zinc-500">v4</span>
          </div>
          <input
            value={workspace.studio.projectName}
            onChange={(e) => onStudioChange({ projectName: e.target.value })}
            className="hidden max-w-[240px] bg-transparent text-xs text-zinc-500 outline-none focus:text-zinc-200 sm:block"
            aria-label="Projectnaam"
          />
        </div>

        <div className="flex items-center gap-1">
          <button onClick={undo} disabled={!undoStack.current.length} className="studio-icon-button" title="Undo">↶</button>
          <button onClick={redo} disabled={!redoStack.current.length} className="studio-icon-button" title="Redo">↷</button>
          <button onClick={() => setPromptOpen(true)} className="studio-secondary-button hidden sm:block">Prompt Inspector</button>
        </div>
      </header>

      <div className="flex min-h-0 flex-1">
        <StudioNavigator active={section} onSelect={openSection} />

        <main className="relative flex min-w-0 flex-1 flex-col bg-[#090b0f]">
          <div className="absolute left-3 top-3 z-20 flex rounded-xl border border-white/5 bg-black/40 p-1 backdrop-blur-md">
            <button onClick={() => setPreviewMode("image")} className={`studio-tab ${previewMode === "image" ? "studio-tab-active" : ""}`}>Image</button>
            <button onClick={() => setPreviewMode("pose")} className={`studio-tab ${previewMode === "pose" ? "studio-tab-active" : ""}`}>Pose</button>
          </div>

          <div className="relative flex min-h-0 flex-1 items-center justify-center overflow-hidden p-3 sm:p-6">
            <div className="studio-grid-bg absolute inset-0 pointer-events-none" />
            {previewMode === "pose" ? (
              <div className="relative z-10 w-full max-w-2xl">
                <PoseVisualizer state={contractPreview.effectiveState} />
              </div>
            ) : activeRecord ? (
              <div className="group relative z-10 flex h-full w-full items-center justify-center">
                <img
                  src={activeRecord.imageData}
                  alt="Feet Studio render"
                  className="max-h-full max-w-full rounded-2xl object-contain shadow-2xl shadow-black/70"
                />
                <div className="absolute right-3 top-3 flex gap-2">
                  <button onClick={handleShare} className="studio-floating-button" title="Delen">↗</button>
                  <button onClick={handleDownload} className="studio-floating-button" title="PNG downloaden">↓</button>
                  <button onClick={() => setFixMenuOpen(!fixMenuOpen)} className="studio-floating-button" title="Targeted Fix">✦</button>
                </div>

                {fixMenuOpen && (
                  <div className="absolute right-3 top-16 w-52 rounded-2xl border border-white/10 bg-[#11141a]/95 p-2 shadow-2xl backdrop-blur-xl">
                    {(["anatomy", "hosiery", "nails", "footwear", "pose", "realism"] as FixTarget[]).map((target) => (
                      <button key={target} onClick={() => void handleFix(target)} className="w-full rounded-xl px-3 py-2 text-left text-xs capitalize text-zinc-400 hover:bg-white/5 hover:text-white">
                        Fix {target}
                      </button>
                    ))}
                  </div>
                )}

                {activeRecord.inspector && (
                  <div className="absolute bottom-3 left-3 max-w-sm rounded-2xl border border-white/5 bg-black/55 p-3 backdrop-blur-lg">
                    <div className="text-[10px] font-bold uppercase tracking-wider text-zinc-400">AI Inspector</div>
                    <div className="mt-1 text-xs text-zinc-300">{activeRecord.inspector.summary}</div>
                    {activeRecord.inspector.issues.length > 0 && (
                      <div className="mt-2 text-[10px] text-amber-300">{activeRecord.inspector.issues[0]}</div>
                    )}
                  </div>
                )}
              </div>
            ) : (
              <div className="relative z-10 max-w-sm text-center">
                <div className="mx-auto grid h-20 w-20 place-items-center rounded-3xl border border-white/5 bg-white/[0.025] text-3xl text-zinc-700">◒</div>
                <h2 className="mt-5 text-sm font-bold text-zinc-300">Studio ready</h2>
                <p className="mt-1 text-xs leading-relaxed text-zinc-600">Kies je look, controleer de resolver en genereer je eerste v4 render.</p>
              </div>
            )}

            {isGenerating && (
              <div className="absolute inset-0 z-40 grid place-items-center bg-black/70 backdrop-blur-sm">
                <div className="rounded-3xl border border-white/10 bg-[#11141a] px-8 py-6 text-center shadow-2xl">
                  <div className="mx-auto h-9 w-9 animate-spin rounded-full border-2 border-fuchsia-400 border-t-transparent" />
                  <div className="mt-4 text-xs font-semibold text-zinc-200">{progress || "Working..."}</div>
                  <div className="mt-1 text-[10px] text-zinc-600">Succesvolle stappen blijven bewaard.</div>
                </div>
              </div>
            )}

            {error && (
              <div className="absolute bottom-4 left-4 right-4 z-50 rounded-2xl border border-red-500/20 bg-red-950/90 p-4 text-xs text-red-100 shadow-xl sm:left-auto sm:max-w-md">
                <div className="font-bold">Renderfout</div>
                <div className="mt-1 text-red-200/70">{error}</div>
              </div>
            )}
          </div>

          <RenderBar
            studio={workspace.studio}
            isGenerating={isGenerating}
            onGenerate={() => void handleGenerate()}
            onStop={() => {
              stopAfterCurrent.current = true;
              setToast("Stopt na de huidige call.");
            }}
            onInspectPrompt={() => setPromptOpen(true)}
            warningCount={readiness.warnings.length}
            adjustmentCount={readiness.adjustments.length}
          />
          <GalleryStrip
            records={gallery}
            activeId={activeRecord?.id}
            onSelect={(record) => {
              setActiveRecord(record);
              setPreviewMode("image");
            }}
          />
        </main>

        <div className="hidden w-[350px] shrink-0 border-l border-white/5 bg-[#101319] md:block">
          <ControlPanel
            section={section}
            state={workspace.design}
            studio={workspace.studio}
            references={references}
            onChange={onChange}
            onStudioChange={onStudioChange}
            onReferencesChange={setReferences}
          />
        </div>
      </div>

      <div className="studio-mobile-nav md:hidden">
        {MOBILE_NAV_ITEMS.map((item) => (
          <button key={item.id} onClick={() => openSection(item.id)} className={section === item.id ? "text-fuchsia-400" : ""}>
            <span className="text-base">{item.icon}</span>
            <span>{item.label}</span>
          </button>
        ))}
      </div>

      {mobilePanelOpen && (
        <div className="fixed inset-0 z-[70] bg-black/55 backdrop-blur-sm md:hidden" onClick={() => setMobilePanelOpen(false)}>
          <div className="absolute bottom-16 left-2 right-2 max-h-[76vh] overflow-hidden rounded-3xl border border-white/10 bg-[#101319] shadow-2xl" onClick={(e) => e.stopPropagation()}>
            <ControlPanel
              section={section}
              state={workspace.design}
              studio={workspace.studio}
              references={references}
              onChange={onChange}
              onStudioChange={onStudioChange}
              onReferencesChange={setReferences}
              onCloseMobile={() => setMobilePanelOpen(false)}
            />
          </div>
        </div>
      )}

      <PromptInspector
        open={promptOpen}
        state={workspace.design}
        studio={workspace.studio}
        references={references}
        onClose={() => setPromptOpen(false)}
      />
    </div>
  );
};

export default App;
