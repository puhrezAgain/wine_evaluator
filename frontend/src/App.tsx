import { BrowserRouter, Route, Routes } from "react-router-dom";
import "./App.css";

import AnalysisPage from "./pages/AnalysisPage";
import WineAnalyzerPage from "./pages/WineAnalyzerPage";
import WineDiagnosisPage from "./pages/WineDiagnosisPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<WineAnalyzerPage />} />
        <Route path="/winelist/:id" element={<AnalysisPage />} />
        <Route path="/diagnose" element={<WineDiagnosisPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
