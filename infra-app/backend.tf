resource "google_cloud_run_service" "api" {
  name     = "wine-evaluator-api-${var.env}"
  location = var.region

  lifecycle {
    prevent_destroy = true
  }

  template {
    spec {
      service_account_name = var.backend_service_account_email

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

resource "google_cloud_run_service_iam_member" "api_public" {
  service  = google_cloud_run_service.api.name
  location = google_cloud_run_service.api.location
  role     = "roles/run.invoker"
  member   = "allUsers"
}
