resource "google_project_iam_member" "vision_usage" {
  project = var.project_id
  role    = "roles/serviceusage.serviceUsageConsumer"
  member  = "serviceAccount:${google_service_account.backend.email}"

  depends_on = [google_project_service.vision]
}
