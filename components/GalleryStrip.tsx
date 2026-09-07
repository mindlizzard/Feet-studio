import React from "react";
import { RenderRecord } from "../typesV4";

interface Props {
  records: RenderRecord[];
  activeId?: string;
  onSelect: (record: RenderRecord) => void;
}

const GalleryStrip: React.FC<Props> = ({ records, activeId, onSelect }) => {
  if (!records.length) return null;

  return (
    <div className="border-t border-white/5 bg-[#090b0f] px-3 py-2">
      <div className="flex gap-2 overflow-x-auto pb-1">
        {records.slice(0, 30).map((record) => (
          <button
            key={record.id}
            onClick={() => onSelect(record)}
            className={`group relative h-16 w-16 shrink-0 overflow-hidden rounded-xl border transition ${
              record.id === activeId
                ? "border-fuchsia-400 ring-2 ring-fuchsia-500/20"
                : "border-white/5 opacity-70 hover:opacity-100"
            }`}
          >
            <img src={record.imageData} alt="" className="h-full w-full object-cover" />
            {record.parentRenderId && <span className="absolute left-1 top-1 rounded bg-black/70 px-1 text-[8px] text-zinc-300">FIX</span>}
            {record.inspector?.issues?.length ? (
              <span className="absolute bottom-1 right-1 grid h-4 w-4 place-items-center rounded-full bg-amber-500 text-[8px] font-bold text-black">
                {record.inspector.issues.length}
              </span>
            ) : null}
          </button>
        ))}
      </div>
    </div>
  );
};

export default GalleryStrip;
