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

TF_VARS_COMMON = \
	-var="env=$(ENV)" \
	-var="project_id=$(PROJECT_ID)"

TF_VARS_APP = \
	$(TF_VARS_COMMON) \
	-var="api_image=$(IMAGE)" \
	-var="backend_service_account_email=$(BACKEND_SA)"


# =========================
# Meta
# =========================
.DEFAULT_GOAL := help

.PHONY: help dev build \
	build-frontend build-backend api-image \
	infra-bootstrap infra-app \
	deploy deploy-backend sync-frontend deploy-frontend provision-and-deploy \
	status logs-backend check-auth open-frontend confirm

help:
	@echo ""
	@echo "Available targets:"
	@echo "  dev                Run frontend + backend locally"
	@echo "  build              Build frontend and backend"
	@echo "  infra-bootstrap    Provision platform/infrastructure (including frontend hosting)"
	@echo "  infra-app          Provision app runtime"
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
	cd frontend && npm ci
	$(eval API_BASE := $(shell terraform -chdir=infra-app output -raw api_url))
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
infra-bootstrap:
	terraform -chdir=infra-bootstrap init
	terraform -chdir=infra-bootstrap apply $(TF_VARS_COMMON)

infra-app: guard-backend-sa api-image
	terraform -chdir=infra-app init
ifeq ($(CONFIRM),true)
	terraform -chdir=infra-app apply $(TF_VARS_APP)
else
	terraform -chdir=infra-app apply -auto-approve -input=false $(TF_VARS_APP)
endif

# =========================
# Helpers
# =========================
status:
	@echo "Environment: $(ENV)"
	@echo "API:"
	@terraform -chdir=infra-app output -raw api_url
	@echo ""
	@echo "Frontend:"
	@terraform -chdir=infra-bootstrap output -raw spa_url

logs-backend:
	gcloud run services logs read $(BACKEND_NAME) \
		--region $(REGION) \
		--project $(PROJECT_ID)

open-frontend:
	open $(shell terraform -chdir=infra-bootstrap output -raw spa_url)

# =========================
# Deploy
# =========================
sync-frontend:
	$(eval SPA_BUCKET := $(shell terraform -chdir=infra-bootstrap output -raw spa_bucket_name))
	gsutil -m rsync -r -d frontend/dist gs://$(SPA_BUCKET)

deploy-frontend: build-frontend sync-frontend

deploy-backend: guard-backend-sa infra-app

deploy: check-auth confirm deploy-backend deploy-frontend

provision-and-deploy:
	@echo "⚠️  This provisions IAM and infrastructure. Admin use only."
	@$(MAKE) infra-bootstrap CONFIRM=true
	@$(MAKE) deploy CONFIRM=true

# =========================
# Guards
# =========================
guard-backend-sa:
ifndef BACKEND_SA
	@echo "Resolving BACKEND_SA from terraform output..."
	$(eval BACKEND_SA := $(shell terraform -chdir=infra-bootstrap output -raw backend_service_account_email))
endif

ifndef BACKEND_SA
	$(error BACKEND_SA is not set and infra-bootstrap has not been applied)
endif

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

ifeq ($(CONFIRM),true)
check-auth:
	@gcloud auth application-default print-access-token > /dev/null 2>&1 || \
		(echo "\033[31mError: ADC credentials expired.\033[0m Run: gcloud auth application-default login"; exit 1)
	@gcloud auth application-default set-quota-project $(PROJECT_ID)
else
check-auth:
	@true
endif