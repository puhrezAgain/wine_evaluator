interface Props {
  message: string;
}

export function ErrorBanner({ message }: Props) {
  return (
    <div style={{ color: "white", background: "crimson", padding: "8px" }}>
      {message}
    </div>
  );
}
