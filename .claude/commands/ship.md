# Ship — Git Feature Branch Workflow

Push current changes to GitHub via a feature branch and open a PR to main.

## Steps

1. **Verify git state** — run `git status` and `git diff --stat` to understand what changed.

2. **Derive a branch name** — if `$ARGUMENTS` is provided, slugify it into `feature/<slug>` (e.g. "add bookmarks" → `feature/add-bookmarks`). If no argument, infer a short name from the changed files/commit context.

3. **Check out the feature branch** — if already on a feature branch, stay there. If on `main`, create and switch:
   ```
   git checkout -b feature/<slug>
   ```

4. **Stage and commit** — stage relevant files (prefer explicit paths over `git add .`), then commit following the project style: imperative mood, concise subject line, Co-Authored-By trailer:
   ```
   git commit -m "$(cat <<'EOF'
   <subject line>"
   ```

5. **Push to origin**:
   ```
   git push -u origin HEAD
   ```

6. **Open a PR to main** using `gh pr create`:
   ```
   gh pr create --base main --title "<title>" --body "$(cat <<'EOF'
   ## Summary
   - <bullet points describing what changed and why>

   ## Test plan
   - [ ] Android emulator
   - [ ] iOS simulator
   - [ ] Web (JS)

   
   )"
   ```

7. **Report the PR URL** so the user can review it.

## Repo link:
https://github.com/sunilk0/BibleAppKMP.git/
## Rules

- Never push directly to `main`.
- Never force-push unless the user explicitly asks.
- Never skip hooks (`--no-verify`).
- Don't commit: `.env`, `local.properties`, `google-services.json`, `GoogleService-Info.plist`, `*.keystore`.
- If there are no changes to commit, tell the user and stop.
- If `gh` is not authenticated, tell the user to run `gh auth login` first.

