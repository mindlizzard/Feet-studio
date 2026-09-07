import React from "react";
import {
  ReferenceAsset,
  ReferenceRole,
  ReferenceStrength,
} from "../typesV4";

interface Props {
  references: ReferenceAsset[];
  onChange: (next: ReferenceAsset[]) => void;
}

const roleOptions: ReferenceRole[] = [
  "foot-shape",
  "skin",
  "nails",
  "hosiery",
  "footwear",
  "pose",
  "camera",
  "scene",
  "style",
];

const strengthOptions: ReferenceStrength[] = [
  "exact",
  "strong",
  "guided",
  "inspiration",
];

const uid = () =>
  typeof crypto !== "undefined" && "randomUUID" in crypto
    ? crypto.randomUUID()
    : `${Date.now()}-${Math.random().toString(36).slice(2)}`;

const fileToDataUrl = (file: File): Promise<string> =>
  new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onerror = () => reject(reader.error);
    reader.onload = () => resolve(String(reader.result || ""));
    reader.readAsDataURL(file);
  });

const ReferenceManager: React.FC<Props> = ({ references, onChange }) => {
  const addFiles = async (files: FileList | null) => {
    if (!files?.length) return;
    const room = Math.max(0, 5 - references.length);
    const selected = Array.from(files).slice(0, room);
    const created: ReferenceAsset[] = [];

    for (const file of selected) {
      if (!file.type.startsWith("image/")) continue;
      created.push({
        id: uid(),
        name: file.name,
        mimeType: file.type,
        dataUrl: await fileToDataUrl(file),
        role: "foot-shape",
        strength: "strong",
        enabled: true,
      });
    }

    onChange([...references, ...created].slice(0, 5));
  };

  const update = (id: string, patch: Partial<ReferenceAsset>) =>
    onChange(references.map((ref) => (ref.id === id ? { ...ref, ...patch } : ref)));

  return (
    <div className="space-y-3">
      <label className="block cursor-pointer rounded-2xl border border-dashed border-white/10 bg-white/[0.025] p-5 text-center hover:border-fuchsia-500/40 hover:bg-fuchsia-500/[0.04]">
        <input
          className="hidden"
          type="file"
          accept="image/*"
          multiple
          onChange={(e) => void addFiles(e.target.files)}
        />
        <div className="text-2xl text-zinc-500">⊕</div>
        <div className="mt-1 text-sm font-semibold text-zinc-200">Referentie toevoegen</div>
        <div className="mt-1 text-[11px] text-zinc-500">Maximaal 5 actieve afbeeldingen in deze v4-flow.</div>
      </label>

      {references.map((ref) => (
        <div key={ref.id} className="rounded-2xl border border-white/5 bg-white/[0.025] p-3">
          <div className="flex gap-3">
            <img src={ref.dataUrl} alt="" className="h-16 w-16 rounded-xl object-cover ring-1 ring-white/10" />
            <div className="min-w-0 flex-1">
              <div className="truncate text-xs font-semibold text-zinc-200">{ref.name}</div>
              <div className="mt-2 grid grid-cols-2 gap-2">
                <select
                  value={ref.role}
                  onChange={(e) => update(ref.id, { role: e.target.value as ReferenceRole })}
                  className="studio-select"
                >
                  {roleOptions.map((role) => <option key={role} value={role}>{role}</option>)}
                </select>
                <select
                  value={ref.strength}
                  onChange={(e) => update(ref.id, { strength: e.target.value as ReferenceStrength })}
                  className="studio-select"
                >
                  {strengthOptions.map((strength) => <option key={strength} value={strength}>{strength}</option>)}
                </select>
              </div>
            </div>
          </div>
          <div className="mt-3 flex items-center justify-between">
            <button
              onClick={() => update(ref.id, { enabled: !ref.enabled })}
              className={`studio-chip ${ref.enabled ? "studio-chip-active" : ""}`}
            >
              {ref.enabled ? "Actief" : "Uit"}
            </button>
            <button
              onClick={() => onChange(references.filter((item) => item.id !== ref.id))}
              className="text-[11px] text-zinc-500 hover:text-red-300"
            >
              Verwijderen
            </button>
          </div>
        </div>
      ))}
    </div>
  );
};

export default ReferenceManager;
