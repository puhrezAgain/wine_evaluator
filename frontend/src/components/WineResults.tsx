import type { WineResult } from "../api/analysis";

interface Props {
  matches: WineResult[];
}

export function WineResults({ matches }: Props) {
  if (matches.length == 0) {
    return <p>No comparable wines found yet :)</p>;
  }

  return (
    <table>
      <thead>
        <tr>
          <th>Match</th>
          <th>Ref Price</th>
          <th>Your Price</th>
          <th>Diff €</th>
          <th>Diff %</th>
          <th>Match %</th>
        </tr>
      </thead>
      <tbody>
        {matches
          .filter((m) => m.type === "MATCH")
          .map((m, i) => (
            <tr key={i}>
              <td>{Array.from(m.matchTokens).join(" ")}</td>
              <td>{m.referencePrice} €</td>
              <td>{m.price} €</td>
              <td>{m.delta} €</td>
              <td>{m.deltaPercent.toFixed(1)} %</td>
              <td>{Math.round(m.jaccard * 100)}%</td>
            </tr>
          ))}
      </tbody>
    </table>
  );
}
