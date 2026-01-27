import { useState } from "react";

interface Props {
  onSubmit: (wine: string, price: number) => void;
  loading: boolean;
}

export function WineQueryForm({ onSubmit, loading }: Props) {
  const [wine, setWine] = useState("");
  const [price, setPrice] = useState("");

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    onSubmit(wine, Number(price));
  }

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <label>Wine</label>
        <input
          value={wine}
          onChange={(e) => setWine(e.target.value)}
          placeholder="Viña Tondonía"
          required
        />
      </div>
      <div>
        <label>Price €</label>
        <input
          type="number"
          min="1"
          value={price}
          onChange={(e) => setPrice(e.target.value)}
          required
        />
      </div>

      <button type="submit" disabled={loading}>
        {loading ? "Analyzing..." : "Analyze"}
      </button>
    </form>
  );
}
