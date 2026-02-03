import { useState } from "react";
import { diagnoseFile, type PriceSignal } from "../api/analysis";

export default function WineDiagnosisPage() {
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [signals, setSignals] = useState<PriceSignal[]>([]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();

    if (!file) return;

    setLoading(true);
    setError(null);

    try {
      const result = await diagnoseFile(file);
      setSignals(result.signals);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to upload wine list");
    } finally {
      setLoading(false);
    }
  }
  return (
    <main>
      <h1>Wine Diagnosis</h1>
      <form onSubmit={handleSubmit}>
        <input
          type="file"
          accept="application/pdf,image/*"
          onChange={(e) => setFile(e.target.files?.[0] ?? null)}
        />

        <button type="submit" disabled={!file || loading}>
          {loading ? "Uploading..." : "Upload"}
        </button>

        {error && <p style={{ color: "red" }}>{error}</p>}

        {signals && (
          <table>
            <thead>
              <tr>
                <th>Raw Line</th>
                <th>Tokens</th>
                <th>Price Hints</th>
              </tr>
            </thead>
            <tbody>
              {signals.map((signal, i) => (
                <tr key={i}>
                  <td>{signal.rawLine}</td>
                  <td>{Array.from(signal.tokens).join(" ")}</td>
                  <td>{Array.from(signal.prices).join(" ")}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </form>
    </main>
  );
}
