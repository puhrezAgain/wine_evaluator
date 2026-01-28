import type { WineMatch } from "../../api/analysis";

interface DoneStateProps {
  results: WineMatch[];
}

export function DoneState({ results }: DoneStateProps) {
  const shareUrl = typeof window !== "undefined" ? window.location.href : "";
  return (
    <section>
      <h3>Results</h3>
      <p>
        Shareable link: <code>{shareUrl}</code>
      </p>
      <table>
        <thead>
          <tr>
            <th>Match</th>
            <th>List Price</th>
            <th>Market Price</th>
            <th>Diff %</th>
          </tr>
        </thead>
        <tbody>
          {results.map((r, i) => (
            <tr key={i}>
              <td>{Array.from(r.matchTokens).join(" ")}</td>
              <td>{r.price} €</td>
              <td>{r.referencePrice} €</td>
              <td>{r.deltaPercent.toFixed(1)}%</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
