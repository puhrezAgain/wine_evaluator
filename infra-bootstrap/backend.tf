resource "google_service_account" "backend" {
  account_id   = "wine-backend-${var.env}"
  display_name = "Wine Evaluator Backend"
}

resource "google_project_iam_member" "service_usage" {
  project = var.project_id
  role    = "roles/serviceusage.serviceUsageConsumer"
  member  = "serviceAccount:${google_service_account.backend.email}"
}

resource "google_artifact_registry_repository" "api" {
  provider      = google
  location      = var.region
  repository_id = "api"
  format        = "DOCKER"

  description = "Docker images for Wine Evaluator API"
}
