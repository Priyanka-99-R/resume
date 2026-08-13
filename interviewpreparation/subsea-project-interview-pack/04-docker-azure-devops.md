# 4. Docker · Azure · CI/CD — Interview Q&A tied to Subsea

**Honest framing:** I worked *with* the Docker/Azure setup as a developer — I used the containers to run and integrate my Schedule-Manager work against a realistic backend, and I understand why the setup existed and what it bought us. I wasn't the DevOps owner of the Azure infrastructure, so for deep infra tuning I'd defer to that specialist. But I can explain the workflow clearly because I lived in it daily.

**Stack:** .NET Web API + Angular frontend, **containerized with Docker**, deployed on **Azure**.

---

## Q1. What is Docker and what problem did it solve on Subsea?

**Answer.** **Docker** packages an application and *everything it needs to run* — runtime, dependencies, config — into an **image**, and runs it as an isolated **container**. The image is the same wherever it runs, so you get a **reproducible environment**.

The problem it kills is the classic **"works on my machine."** Without containers, my laptop, a teammate's laptop, CI, and production can each have subtly different .NET runtime versions, dependencies, or OS libraries — and bugs appear in one place but not another. With Docker, the **same image** runs in all of them, so environment drift disappears.

**In Subsea.** Docker gave us that reproducible environment — the same image ran on my machine, in CI, and on Azure. That eliminated the whole class of environment-drift bugs, especially around the **.NET runtime and its dependencies**. Concretely, I used the Dockerfile/compose setup to spin up the API and its dependencies locally so I could **integrate my Angular frontend against a realistic backend** instead of mocks — which caught integration issues early.

---

## Q2. Image vs container? And Dockerfile vs compose?

**Answer.**
- **Image** — the immutable, built template (app + dependencies). Like a class.
- **Container** — a running instance of an image. Like an object.
- **Dockerfile** — the recipe that *builds* an image, step by step (base image, copy code, restore, build, define the start command).
- **docker-compose** — defines and runs **multiple containers together** (e.g. the API + its dependencies) with one command and shared networking — great for spinning up a realistic local stack.

**In Subsea.** The **Dockerfile** built the .NET API image; **compose** let me bring up the API and what it depended on locally as one stack so my frontend had a real backend to talk to.

---

## Q3. Walk me through a Dockerfile for the .NET API.

**Answer.** A **multi-stage build** — build in an SDK image, then copy just the published output into a small runtime image so the final image is lean and doesn't ship the whole SDK:

```dockerfile
# --- build stage ---
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY *.csproj ./
RUN dotnet restore                 # restore dependencies (cached layer if csproj unchanged)
COPY . ./
RUN dotnet publish -c Release -o /app/publish

# --- runtime stage ---
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS runtime
WORKDIR /app
COPY --from=build /app/publish .   # only the published output ships
EXPOSE 8080
ENTRYPOINT ["dotnet", "Subsea.Api.dll"]
```

**Why multi-stage matters:** the runtime image is small (no SDK/build tools), which means faster pulls/deploys and a smaller attack surface. The layer ordering (restore before copying all source) also means dependency restores are **cached** and only re-run when the project file changes — faster builds.

**In Subsea.** This is the shape of how the API was containerized — build once, run the same artifact everywhere. The Angular app was similarly built and served as static assets from its own container.

---

## Q4. How did Azure fit in?

**Answer.** **Azure hosted the containers.** The Docker images we built were deployed to and run on Azure, so production ran the *same* image that ran locally and in CI. Azure gives managed hosting for containers (services like Azure App Service / Azure Container Apps / AKS), so we got scaling and hosting without managing raw servers.

**In Subsea.** The flow was: build the container → it runs on Azure in production. The value of Docker + Azure together was **parity** — no "it passed CI but broke in prod" surprises, because prod was running the identical image. Config that differed between environments (connection strings, endpoints) came in through **environment variables**, so the same image was promotable across environments without a rebuild.

---

## Q5. Explain your CI/CD and how you treated the pipeline.

**Answer.** CI/CD automates build → test → deploy so problems are caught before merge and deploys are repeatable. My working model: **on every PR the pipeline lints, builds, and runs tests**, and I treat a **green pipeline as the gate to deploy** — nothing merges or ships on red. On merge, the pipeline builds the Docker image and deploys it to Azure.

```
PR opened → lint + build + unit tests → (green) → review + merge
merge → build Docker image → push to registry → deploy container to Azure
```

**In Subsea.** We used Docker for reproducible builds and deployed containers to Azure, so the same image ran locally, in CI, and in prod — which eliminated environment drift. I worked within pipelines that build and test on every PR, and I treated the green pipeline as the gate before anything went out. That discipline is a big part of why fixes and features shipped safely.

---

## Q6. What does "environment parity" mean and why do you care?

**Answer.** **Environment parity** = local, CI, and production being as identical as possible. I care because divergence is where the nastiest bugs live — the ones that only reproduce "over there." Docker delivers parity by running the **same image** everywhere; environment-specific values are injected as **config/env vars**, not baked into the image, so the *code* is identical and only the *configuration* varies.

**In Subsea.** This was the concrete payoff: because dev/CI/prod ran the same container image, I rarely hit "works on my machine" issues, and when I was fixing a production bug I could reproduce it locally in the same environment shape — which is exactly what my reproduce-first debugging approach depends on (see `05-behavioral-and-stars.md`).

---

## Q7. If asked about scaling/resilience on Azure (honest scope)

**Answer.** At a high level: because the API is **stateless and containerized**, it scales **horizontally** — Azure runs more container instances behind a load balancer under load. Config/secrets come from the environment (Azure's app settings / key vault) rather than the image. That's my working understanding as the developer who built and ran against these containers; the deep infra tuning (autoscale rules, networking, cost) was owned by our DevOps side, and I'd partner with them for that.

**In Subsea.** I stayed honest about this boundary — I owned my module's code and its containerized run, and understood the deployment model; I didn't own the Azure infrastructure config, and I'd say so rather than overclaim.

---

### Rapid-fire recap

| Topic | Subsea answer |
|-------|---------------|
| Docker's value | reproducible image → killed "works on my machine" (esp. .NET runtime drift) |
| Image vs container | image = template, container = running instance |
| Dockerfile | multi-stage: SDK build → lean aspnet runtime image |
| compose | bring up API + deps locally so my Angular UI had a real backend |
| Azure | hosts the containers; prod runs the same image as local/CI |
| CI/CD | PR → lint/build/test → green gates merge → build image → deploy to Azure |
| Env parity | same image everywhere; env vars for config → fewer "works over there" bugs |
| Honest scope | owned code + containerized run; deep Azure infra was DevOps' domain |
