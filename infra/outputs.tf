output "api_url" {
  value = google_cloud_run_service.api.status[0].url
}

output "spa_bucket_name" {
  value = google_storage_bucket.spa.name
}

output "spa_url" {
  value = "http://${google_compute_global_forwarding_rule.spa_http.ip_address}"
}

output "github_wif_provider" {
  value = google_iam_workload_identity_pool_provider.github.name
}

output "ci_service_account_email" {
  value = google_service_account.ci.email
}
