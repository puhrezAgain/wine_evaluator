import type { WineResult } from "../../api/analysis";

interface DoneStateProps {
  results: WineResult[];
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
            <th>Wine</th>
            <th>Match</th>
            <th>List Price</th>
            <th>Market Price</th>
            <th>Diff %</th>
          </tr>
        </thead>
        <tbody>
          {results.map((r, i) => (
            <tr key={i}>
              <td>{Array.from(r.tokens).join(" ")}</td>
              {r.type === "MATCH" ? (
                <>
                  <td>{Array.from(r.matchTokens).join(" ")}</td>
                  <td>{r.price} €</td>
                  <td>{r.delta} €</td>
                  <td>{r.deltaPercent.toFixed(1)}%</td>
                </>
              ) : (
                <>
                  <td>
                    <sup>New Wine!</sup>
                  </td>
                  <td>{r.price} €</td>
                  <td>N/A</td>
                  <td>N/A</td>
                </>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
