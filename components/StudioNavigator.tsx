import React from "react";
import { StudioSection } from "../typesV4";

const items: Array<{ id: StudioSection; icon: string; label: string }> = [
  { id: "feet", icon: "◒", label: "Voeten" },
  { id: "skin", icon: "◉", label: "Huid" },
  { id: "nails", icon: "✦", label: "Nagels" },
  { id: "hosiery", icon: "◇", label: "Beenmode" },
  { id: "shoes", icon: "⌁", label: "Schoenen" },
  { id: "pose", icon: "⌇", label: "Pose" },
  { id: "camera", icon: "▣", label: "Camera" },
  { id: "scene", icon: "▤", label: "Scene" },
  { id: "light", icon: "☼", label: "Licht" },
  { id: "references", icon: "⊕", label: "Refs" },
  { id: "render", icon: "◆", label: "Render" },
];

interface Props {
  active: StudioSection;
  onSelect: (section: StudioSection) => void;
}

const StudioNavigator: React.FC<Props> = ({ active, onSelect }) => (
  <nav className="hidden md:flex w-[86px] shrink-0 flex-col items-center gap-1 border-r border-white/5 bg-[#0b0d11] px-2 py-3">
    <div className="mb-4 h-9 w-9 rounded-xl bg-gradient-to-br from-fuchsia-500 to-indigo-500 p-[1px] shadow-lg shadow-fuchsia-950/20">
      <div className="grid h-full w-full place-items-center rounded-[11px] bg-[#11141a] text-sm font-black text-white">VS</div>
    </div>
    {items.map((item) => (
      <button
        key={item.id}
        onClick={() => onSelect(item.id)}
        className={`group flex w-full flex-col items-center rounded-xl px-1 py-2.5 transition ${
          active === item.id
            ? "bg-white/8 text-white shadow-inner"
            : "text-zinc-500 hover:bg-white/5 hover:text-zinc-200"
        }`}
        title={item.label}
      >
        <span className={`text-lg ${active === item.id ? "text-fuchsia-400" : ""}`}>{item.icon}</span>
        <span className="mt-1 text-[9px] font-semibold tracking-wide">{item.label}</span>
      </button>
    ))}
  </nav>
);

export default StudioNavigator;

export const MOBILE_NAV_ITEMS = items.filter((item) =>
  ["feet", "hosiery", "pose", "camera", "scene", "render"].includes(item.id)
);
