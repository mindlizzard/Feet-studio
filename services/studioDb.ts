import { RenderRecord } from "../typesV4";

const DB_NAME = "feet-studio-v4";
const DB_VERSION = 1;
const STORE = "renders";

const openDb = (): Promise<IDBDatabase> =>
  new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);
    request.onerror = () => reject(request.error);
    request.onupgradeneeded = () => {
      const db = request.result;
      if (!db.objectStoreNames.contains(STORE)) {
        const store = db.createObjectStore(STORE, { keyPath: "id" });
        store.createIndex("createdAt", "createdAt");
      }
    };
    request.onsuccess = () => resolve(request.result);
  });

export const saveRenderRecord = async (record: RenderRecord): Promise<void> => {
  const db = await openDb();
  await new Promise<void>((resolve, reject) => {
    const tx = db.transaction(STORE, "readwrite");
    tx.objectStore(STORE).put(record);
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
  db.close();
};

export const listRenderRecords = async (limit = 60): Promise<RenderRecord[]> => {
  const db = await openDb();
  const result = await new Promise<RenderRecord[]>((resolve, reject) => {
    const tx = db.transaction(STORE, "readonly");
    const request = tx.objectStore(STORE).getAll();
    request.onsuccess = () => resolve((request.result || []) as RenderRecord[]);
    request.onerror = () => reject(request.error);
  });
  db.close();
  return result
    .sort((a, b) => b.createdAt.localeCompare(a.createdAt))
    .slice(0, limit);
};

export const deleteRenderRecord = async (id: string): Promise<void> => {
  const db = await openDb();
  await new Promise<void>((resolve, reject) => {
    const tx = db.transaction(STORE, "readwrite");
    tx.objectStore(STORE).delete(id);
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
  db.close();
};
