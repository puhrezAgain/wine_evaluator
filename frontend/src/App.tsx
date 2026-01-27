import { BrowserRouter, Route, Routes } from "react-router-dom";
import "./App.css";

import AnalysisPage from "./pages/AnalysisPage";
import WineAnalyzerPage from "./pages/WineAnalyzerPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<WineAnalyzerPage />} />
        <Route path="/winelist/:id" element={<AnalysisPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
