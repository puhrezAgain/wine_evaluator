import { useEffect, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import { getAnalysis, type AnalysisResult } from "../api/analysis";
import { DoneState } from "../components/analysis/DoneState";
import FailedState from "../components/analysis/FailedState";
import PendingState from "../components/analysis/PendingState";
import { ErrorBanner } from "../components/ErrorBanner";
import Loading from "../components/Loading";

export default function AnalysisPage() {
  const { id } = useParams<{ id: string }>();

  if (!id) {
    return <ErrorBanner message="Missing analytics id" />;
  }

  // use inner page to ensure we have the id for our analysis
  return <AnalysisPageInner id={id} />;
}

interface AnalysisPageInnerProps {
  id: string;
}
function AnalysisPageInner({ id }: AnalysisPageInnerProps) {
  const [result, setResult] = useState<AnalysisResult | null>(null);
  const [error, setError] = useState<string | null>(null);
  const timerRef = useRef<number | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function fetchAnalysis() {
      if (cancelled) return;

      try {
        const res = await getAnalysis(id);
        setResult(res);

        if (res.status == "PENDING") {
          if (timerRef.current) clearTimeout(timerRef.current);
          timerRef.current = window.setTimeout(fetchAnalysis, 1500);
        }
      } catch (e) {
        setError(e instanceof Error ? e.message : "Failed to load analysis");
      }
    }
    fetchAnalysis();

    return () => {
      cancelled = true;
      if (timerRef.current) clearTimeout(timerRef.current);
    };
  }, [id]);

  if (error) return <ErrorBanner message={error} />;
  if (!result) return <Loading />;

  switch (result.status) {
    case "PENDING":
      return <PendingState id={id} />;

    case "FAILED":
      return <FailedState error={result.error} />;

    case "DONE":
      return <DoneState results={result.results} />;
  }
}
