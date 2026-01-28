resource "google_project_service" "vision" {
  project = var.project_id
  service = "vision.googleapis.com"
}
