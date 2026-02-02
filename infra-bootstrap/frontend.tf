resource "google_storage_bucket" "spa" {
  name     = "wine-evaluator-spa-${var.env}"
  location = var.region

  uniform_bucket_level_access = true

  website {
    main_page_suffix = "index.html"
    not_found_page   = "index.html"
  }
}

resource "google_storage_bucket_iam_member" "public_read" {
  bucket = google_storage_bucket.spa.name
  role   = "roles/storage.objectViewer"
  member = "allUsers"
}

resource "google_compute_backend_bucket" "spa" {
  name        = "wine-evaluator-spa-backend-${var.env}"
  bucket_name = google_storage_bucket.spa.name
  enable_cdn  = var.env == "prod" ? true : false

  # Optional: Only attach custom no-cache headers if NOT in prod
  custom_response_headers = var.env != "prod" ? [
    "Cache-Control: no-store, no-cache, must-revalidate, max-age=0",
    "Pragma: no-cache",
    "Expires: 0"
  ] : []
}

resource "google_compute_url_map" "spa" {
  name            = "wine-evaluator-spa-url-map-${var.env}"
  default_service = google_compute_backend_bucket.spa.id

  host_rule {
    hosts        = ["*"]
    path_matcher = "spa-matcher"
  }

  path_matcher {
    name            = "spa-matcher"
    default_service = google_compute_backend_bucket.spa.id

    route_rules {
      priority = 1

      match_rules {
        prefix_match = "/assets/"
      }

      service = google_compute_backend_bucket.spa.id
    }

    route_rules {
      priority = 2

      match_rules {
        prefix_match = "/"
      }

      route_action {
        url_rewrite {
          path_prefix_rewrite = "/index.html"
        }
      }

      service = google_compute_backend_bucket.spa.id
    }
  }
}

resource "google_compute_target_http_proxy" "spa" {
  name    = "wine-evaluator-spa-http-proxy-${var.env}"
  url_map = google_compute_url_map.spa.id
}

resource "google_compute_global_forwarding_rule" "spa_http" {
  name       = "wine-evaluator-spa-http-${var.env}"
  port_range = "80"
  target     = google_compute_target_http_proxy.spa.id
}
