variable "project_id" {
  type = string
}

variable "env" {
  type    = string
  default = "dev"
}

variable "region" {
  type    = string
  default = "europe-west1"
}


variable "api_image" {
  type = string
}


variable "github_repository" {
  type    = string
  default = "puhrezAgain/wine_evaluator"
}
