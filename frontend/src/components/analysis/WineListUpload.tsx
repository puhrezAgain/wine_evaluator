import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { uploadWineList } from "../../api/analysis";

export function WineListUpload() {
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!file) return;

    setLoading(true);
    setError(null);

    try {
      const result = await uploadWineList(file);
      navigate(`/winelist/${result.record.id}`);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to upload wine list");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section>
      <h2>Check wine list</h2>
      <form onSubmit={handleSubmit}>
        <input
          type="file"
          accept="application/pdf,image/*"
          onChange={(e) => setFile(e.target.files?.[0] ?? null)}
        />

        <button type="submit" disabled={!file || loading}>
          {loading ? "Uploading..." : "Upload"}
        </button>

        {error && <p style={{ color: "red" }}>{error}</p>}
      </form>
    </section>
  );
}
