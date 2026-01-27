interface FailedStateProps {
  error?: string;
}

export default function FailedState({ error }: FailedStateProps) {
  return (
    <div style={{ padding: "2rem", color: "#b00020" }}>
      <h2>❌ Something went wrong</h2>
      <p>{error ?? "Unknown error"}</p>
    </div>
  );
}
