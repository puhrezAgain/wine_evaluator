resource "google_cloud_run_service" "api" {
  name     = "wine-evaluator-api-${var.env}"
  location = var.region

  template {
    spec {
      service_account_name = google_service_account.backend.email

      containers {
        image = var.api_image

        ports {
          container_port = 8080
        }
      }
    }
  }

  traffic {
    percent         = 100
    latest_revision = true
  }

  depends_on = [google_project_service.vision]
}

resource "google_service_account" "backend" {
  account_id   = "wine-backend-${var.env}"
  display_name = "Wine Evaluator Backend"
}

resource "google_project_iam_member" "vision_usage" {
  project = var.project_id
  role    = "roles/serviceusage.serviceUsageConsumer"
  member  = "serviceAccount:${google_service_account.backend.email}"

  depends_on = [google_project_service.vision]
}

resource "google_artifact_registry_repository" "api" {
  provider      = google
  location      = var.region
  repository_id = "api"
  format        = "DOCKER"

  description = "Docker images for Wine Evaluator API"
}

resource "google_cloud_run_service_iam_member" "api_public" {
  service  = google_cloud_run_service.api.name
  location = google_cloud_run_service.api.location
  role     = "roles/run.invoker"
  member   = "allUsers"
}
