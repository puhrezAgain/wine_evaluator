resource "google_service_account" "backend" {
  account_id   = "wine-backend-${var.env}"
  display_name = "Wine Evaluator Backend"
}
