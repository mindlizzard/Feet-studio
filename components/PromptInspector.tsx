import React, { useMemo, useState } from "react";
import { DesignState } from "../types";
import { ReferenceAsset, StudioV4State } from "../typesV4";
import { buildRenderContract, getRenderReadiness } from "../services/renderPipeline";

interface Props {
  open: boolean;
  state: DesignState;
  studio: StudioV4State;
  references: ReferenceAsset[];
  onClose: () => void;
}

const Status = ({ ok, label }: { ok: boolean; label: string }) => (
  <div className="flex items-center justify-between rounded-xl bg-white/[0.03] px-3 py-2 text-xs">
    <span className="text-zinc-400">{label}</span>
    <span className={ok ? "text-emerald-400" : "text-amber-400"}>{ok ? "✓" : "⚠"}</span>
  </div>
);

const PromptInspector: React.FC<Props> = ({ open, state, studio, references, onClose }) => {
  const [showPrompt, setShowPrompt] = useState(false);
  const contract = useMemo(() => buildRenderContract(state, studio, references).contract, [state, studio, references]);
  const readiness = getRenderReadiness(contract);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-[80] grid place-items-center bg-black/70 p-3 backdrop-blur-md">
      <div className="max-h-[92vh] w-full max-w-3xl overflow-y-auto rounded-3xl border border-white/10 bg-[#11141a] shadow-2xl">
        <div className="sticky top-0 flex items-center justify-between border-b border-white/5 bg-[#11141a]/95 px-5 py-4 backdrop-blur">
          <div>
            <h2 className="text-sm font-bold text-white">Prompt Inspector</h2>
            <p className="text-[10px] text-zinc-500">Dit is de effectieve renderstate die werkelijk wordt gecompileerd.</p>
          </div>
          <button onClick={onClose} className="studio-icon-button">×</button>
        </div>

        <div className="grid gap-5 p-5 md:grid-cols-2">
          <div className="space-y-3">
            <div className="studio-card">
              <h3 className="studio-card-title">Render Readiness</h3>
              <div className={`text-lg font-bold ${readiness.ready ? "text-emerald-400" : "text-amber-400"}`}>
                {readiness.ready ? "✓ Ready" : `⚠ ${readiness.blockers.length} blocker(s)`}
              </div>
              <p className="mt-1 text-[10px] text-zinc-500">{readiness.warnings.length} warnings · {readiness.adjustments.length} auto adjustments</p>
            </div>

            <Status ok={contract.facts.toesVisible || !contract.facts.nailsVisible} label="Visibility contract" />
            <Status ok={!contract.facts.isFishnet || !contract.facts.isOpaqueHosiery} label="Fishnet physics" />
            <Status ok={!contract.facts.isClosedShoe || !contract.facts.shoeIsWorn || !contract.facts.nailsVisible} label="Shoe occlusion" />
            <Status ok={readiness.blockers.length === 0} label="Resolver" />

            {contract.decisions.map((decision) => (
              <div key={decision.id} className="rounded-xl border border-white/5 bg-white/[0.02] p-3">
                <div className="text-xs font-semibold text-zinc-200">{decision.title}</div>
                <div className="mt-1 text-[10px] leading-relaxed text-zinc-500">{decision.detail}</div>
              </div>
            ))}
          </div>

          <div className="space-y-3">
            <div className="studio-card">
              <h3 className="studio-card-title">Effective Summary</h3>
              <dl className="grid grid-cols-[90px_1fr] gap-x-3 gap-y-2 text-[11px]">
                <dt className="text-zinc-600">Anatomy</dt><dd className="text-zinc-300">{contract.effectiveState.footShape} · {contract.effectiveState.archType}</dd>
                <dt className="text-zinc-600">Hosiery</dt><dd className="text-zinc-300">{contract.effectiveState.hosieryType} · {contract.effectiveState.hosieryDenier}</dd>
                <dt className="text-zinc-600">Shoes</dt><dd className="text-zinc-300">{contract.effectiveState.footwearType} · {contract.effectiveState.footwearState}</dd>
                <dt className="text-zinc-600">Pose</dt><dd className="text-zinc-300">{contract.effectiveState.customPose || contract.effectiveState.pose}</dd>
                <dt className="text-zinc-600">Camera</dt><dd className="text-zinc-300">{contract.studio.camera.lens} · {contract.effectiveState.customCamera || contract.effectiveState.cameraAngle}</dd>
                <dt className="text-zinc-600">Output</dt><dd className="text-zinc-300">{contract.model} · {contract.resolution} · {contract.aspectRatio}</dd>
                <dt className="text-zinc-600">Refs</dt><dd className="text-zinc-300">{contract.referencePlan.length}</dd>
              </dl>
            </div>

            <button onClick={() => setShowPrompt(!showPrompt)} className="studio-secondary-button w-full">
              {showPrompt ? "Verberg compiled prompt" : "Bekijk compiled prompt"}
            </button>
          </div>
        </div>

        {showPrompt && (
          <div className="border-t border-white/5 p-5">
            <pre className="max-h-[48vh] overflow-auto whitespace-pre-wrap rounded-2xl bg-black/35 p-4 text-[10px] leading-relaxed text-zinc-400">
              {contract.compiledPrompt}
            </pre>
          </div>
        )}
      </div>
    </div>
  );
};

export default PromptInspector;
