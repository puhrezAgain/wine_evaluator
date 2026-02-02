terraform {
  required_version = ">= 1.5"

  backend "gcs" {
    bucket = "wine-evaluator-tfstate"
    prefix = "infra-app"
  }

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
}
