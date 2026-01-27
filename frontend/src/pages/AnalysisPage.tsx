import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getAnalysis, type AnalysisResult } from "../api/analysis";
import { DoneState } from "../components/DoneState";
import { ErrorBanner } from "../components/ErrorBanner";
import FailedState from "../components/FailedState";
import Loading from "../components/Loading";
import PendingState from "../components/PendingState";

export default function AnalysisPage() {
  const { id } = useParams<{ id: string }>();
  const [result, setResult] = useState<AnalysisResult | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;

    let timer: number;

    async function fetchAnalysis() {
      try {
        const res = await getAnalysis(id!!);
        setResult(res);

        if (res.status == "PENDING") {
          timer = window.setTimeout(fetchAnalysis, 1500);
        }
      } catch (e: any) {
        setError("Failed to load analysis");
      }
    }
    fetchAnalysis();
    return () => clearTimeout(timer);
  }, [id]);

  if (error) return <ErrorBanner message={error} />;
  if (!result) return <Loading />;

  switch (result.status) {
    case "PENDING":
      return <PendingState id={id!!} />;

    case "FAILED":
      return <FailedState error={result.error} />;

    case "DONE":
      return <DoneState results={result.results} />;
  }
}
