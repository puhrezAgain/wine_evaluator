# =========================
# Global config
# =========================
PROJECT_ID ?= wine-evaluator
REGION ?= europe-west1

CONFIRM ?= true
ENV ?= dev

BACKEND_NAME = wine-evaluator-api-$(ENV)
FRONTEND_NAME = wine-ui-$(ENV)

IMAGE_TAG := $(shell git rev-parse --short HEAD)
IMAGE := $(REGION)-docker.pkg.dev/$(PROJECT_ID)/api/wine-evaluator-api-$(ENV):$(IMAGE_TAG)

# Cloud Run common flags
CLOUD_RUN_FLAGS = \
	--region $(REGION) \
	--project $(PROJECT_ID) \
	--allow-unauthenticated

TF_VARS = \
	-var="env=$(ENV)" \
	-var="api_image=$(IMAGE)" \
	-var="project_id=$(PROJECT_ID)" \

# =========================
# Meta
# =========================
.DEFAULT_GOAL := help

.PHONY: help dev build \
	build-frontend build-backend api-image \
	infra-init infra-plan infra-apply \
	deploy deploy-backend sync-frontend deploy-frontend deploy-ci  \
	status logs-backend check-auth open-frontend confirm

help:
	@echo ""
	@echo "Available targets:"
	@echo "  dev                Run frontend + backend locally"
	@echo "  build              Build frontend and backend"
	@echo "  infra-init         Terraform init"
	@echo "  infra-apply        Terraform apply (env=$(ENV))"
	@echo "  infra-plan         Terraform plan (env=$(ENV))"
	@echo "  status             Verify cloud deployment status"
	@echo "  logs-backend       Read backend logs from Cloud Run"
	@echo "  deploy-frontend    Deploy frontend to Cloud Storage"
	@echo "  deploy-backend     Deploy backend Cloud Run via Terraform"
	@echo "  deploy             Deploy frontend + backend"
	@echo "  open-frontend      Open the frontend in the browser"
	@echo ""


# =========================
# Dev & Build
# =========================
dev:
	npm run dev

build: build-frontend build-backend

build-frontend:
	$(eval API_BASE := $(shell terraform -chdir=infra output -raw api_url))
	@echo "Building frontend with API_BASE=$(API_BASE)"
	cd frontend && VITE_API_BASE="$(API_BASE)" npm run build

build-backend:
	cd backend && ./gradlew clean bootJar --no-daemon

api-image: build-backend
	docker build --platform=linux/amd64 -t $(IMAGE) backend/
	docker push $(IMAGE)


# =========================
# Infrastructure
# =========================
infra-init:
	terraform -chdir=infra init

infra-plan:
	terraform -chdir=infra plan $(TF_VARS)

infra-apply: api-image
	terraform -chdir=infra apply $(TF_VARS)


# =========================
# Helpers
# =========================
status:
	@echo "API:"
	@terraform -chdir=infra output -raw api_url
	@echo ""
	@echo "Frontend:"
	@terraform -chdir=infra output -raw spa_url

logs-backend:
	gcloud run services logs read $(BACKEND_NAME) \
		--region $(REGION) \
		--project $(PROJECT_ID)

open-frontend:
	open $(shell terraform -chdir=infra output -raw spa_url)

# =========================
# Deploy
# =========================
sync-frontend:
	$(eval SPA_BUCKET := $(shell terraform -chdir=infra output -raw spa_bucket_name))
	gsutil -m rsync -r -d frontend/dist gs://$(SPA_BUCKET)

deploy-frontend: build-frontend sync-frontend

deploy-backend: infra-apply

deploy: check-auth confirm deploy-backend deploy-frontend

deploy-ci: deploy-backend deploy-frontend

# =========================
# Meta
# =========================
ifeq ($(CONFIRM),true)
confirm:
	@read -p "Deploy to $(ENV) in project $(PROJECT_ID)? [y/N] " ans; \
	if [ "$$ans" != "y" ]; then \
		echo "Aborted."; exit 1; \
	fi
else
confirm:
	@true
endif

check-auth:
	@gcloud auth application-default print-access-token > /dev/null 2>&1 || \
		(echo "\033[31mError: ADC credentials expired.\033[0m Run: gcloud auth application-default login"; exit 1)
	@gcloud auth application-default set-quota-project $(PROJECT_ID)