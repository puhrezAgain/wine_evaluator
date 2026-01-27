import { useState } from "react";
import { analyzeWine, type WineMatch } from "../api/analysis";
import { ErrorBanner } from "../components/ErrorBanner";
import { WineListUpload } from "../components/WineListUpload";
import { WineQueryForm } from "../components/WineQueryForm";
import { WineResults } from "../components/WineResults";

export default function WineAnalyzerPage() {
  const [matches, setMatches] = useState<WineMatch[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleAnalyze(wine: string, price: number) {
    try {
      setError(null);
      setLoading(true);
      const result = await analyzeWine(wine, price);
      setMatches(result.matches);
    } catch (e: any) {
      setError(e.message);
      setMatches([]);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main>
      <h1>Wine Reader</h1>

      {error && <ErrorBanner message={error} />}
      <WineQueryForm onSubmit={handleAnalyze} loading={loading} />
      {matches && <WineResults matches={matches} />}

      <hr />

      <WineListUpload />
    </main>
  );
}
