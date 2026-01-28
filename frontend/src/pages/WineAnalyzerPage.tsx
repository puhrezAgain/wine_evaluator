import { useState } from "react";
import { analyzeWine, type WineMatch } from "../api/analysis";
import { WineListUpload } from "../components/analysis/WineListUpload";
import { WineQueryForm } from "../components/analysis/WineQueryForm";
import { ErrorBanner } from "../components/ErrorBanner";
import { WineResults } from "../components/WineResults";

export default function WineAnalyzerPage() {
  const [matches, setMatches] = useState<WineMatch[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [hasSearched, setHasSearched] = useState(false);

  async function handleAnalyze(wine: string, price: number) {
    try {
      setHasSearched(true);
      setError(null);
      setLoading(true);
      const result = await analyzeWine(wine, price);
      setMatches(result.matches);
    } catch (e) {
      setError(e instanceof Error ? e.message : "error handling alanyze");
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
      {hasSearched && <WineResults matches={matches} />}

      <hr />

      <WineListUpload />
    </main>
  );
}
