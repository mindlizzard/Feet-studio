import React from "react";
import { StudioV4State } from "../typesV4";

interface Props {
  studio: StudioV4State;
  isGenerating: boolean;
  onGenerate: () => void;
  onStop: () => void;
  onInspectPrompt: () => void;
  warningCount: number;
  adjustmentCount: number;
}

const RenderBar: React.FC<Props> = ({
  studio,
  isGenerating,
  onGenerate,
  onStop,
  onInspectPrompt,
  warningCount,
  adjustmentCount,
}) => {
  const estimatedCalls =
    studio.batchCount + (studio.inspectorMode === "off" ? 0 : studio.batchCount);

  return (
    <div className="border-t border-white/5 bg-[#0c0f14]/95 px-3 py-2 backdrop-blur-xl">
      <div className="mx-auto flex max-w-5xl items-center gap-2">
        <button onClick={onInspectPrompt} className="studio-secondary-button hidden sm:flex">
          {warningCount ? `⚠ ${warningCount}` : "✓ Ready"}
          {adjustmentCount ? ` · ${adjustmentCount} fixes` : ""}
        </button>

        <div className="hidden min-w-0 flex-1 items-center gap-2 md:flex">
          <span className="studio-badge">{studio.renderMode}</span>
          <span className="studio-badge">{studio.resolution}</span>
          <span className="studio-badge">{studio.aspectRatio}</span>
          <span className="studio-badge">×{studio.batchCount}</span>
          <span className="truncate text-[10px] text-zinc-600">± {estimatedCalls} AI calls incl. inspector</span>
        </div>

        {isGenerating ? (
          <button onClick={onStop} className="studio-danger-button ml-auto">
            Stop na huidige
          </button>
        ) : (
          <button onClick={onGenerate} className="studio-primary-button ml-auto min-w-[150px]">
            ✦ Generate
          </button>
        )}
      </div>
    </div>
  );
};

export default RenderBar;
