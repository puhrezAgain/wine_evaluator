export const API_BASE = import.meta.env.VITE_API_BASE ?? "";

export type WineMatch = {
  queryUploadId: string;
  jaccard: number;
  price: number;
  referencePrice: number;
  delta: number;
  deltaPercent: number;
  matchTokens: string[];
  tokens: string[];
};

export type WineQueryResponse = {
  original: string;
  queryPrice: number;
  matches: WineMatch[];
};

export async function analyzeWine(
  wine: string,
  price: number,
): Promise<WineQueryResponse> {
  const res = await fetch(`${API_BASE}/analysis`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ wine, price }),
  });

  if (!res.ok) {
    const err = await res.json();
    throw new Error(err.message || "Request failed");
  }

  const json = await res.json();
  return json.results;
}

export interface AnalysisStarted {
  record: {
    id: string;
    status: "PENDING";
  };
}

export async function uploadWineList(file: File): Promise<AnalysisStarted> {
  const form = new FormData();
  form.append("file", file);

  const res = await fetch(`${API_BASE}/analysis`, {
    method: "POST",
    body: form,
  });

  if (!res.ok) {
    throw new Error("Upload failed");
  }

  return res.json();
}

export type AnalysisResult =
  | {
      id: string;
      status: "PENDING";
    }
  | {
      id: string;
      status: "FAILED";
      error?: string;
    }
  | {
      id: string;
      status: "DONE";
      results: WineMatch[];
    };

export async function getAnalysis(id: string): Promise<AnalysisResult> {
  const res = await fetch(`${API_BASE}/analysis/${id}`);
  const data = await res.json();

  if (res.status === 202) {
    return { id: data.id, status: "PENDING" };
  }

  if (res.status === 422) {
    return { id: data.id, status: "FAILED", error: data.error };
  }

  if (res.status === 200) {
    return { id: data.id, status: "DONE", results: data.results };
  }

  throw new Error("Failed to fetch analysis");
}
