interface PendingStateProps {
  id: string;
}
export default function PendingState({ id }: PendingStateProps) {
  return (
    <>
      <h1>Analyzing wine list…</h1>
      <p>This page will update automatically.</p>
    </>
  );
}
