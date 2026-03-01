# Multimedia File Manager

Short JavaFX-based multimedia file manager used for managing documents, categories and users.

**Prerequisites**
- Java JDK 21 (LTS) installed and `JAVA_HOME` set.
- Maven installed (`mvn`).
- Do not commit local JavaFX SDKs into the repo (see "Large files").

**Quick setup**
- Install JDK 21 and Maven.

- To run tests:

```bash
mvn test
```

- To run the app (JavaFX runtime must be available on your system):

```bash
mvn clean javafx:run
```

or build a shaded jar and run it:

```bash
mvn package
java -jar target/*.jar
```

**Upgrade to Java 21 (developer steps)**
- Update `pom.xml` properties:
  - Set `<maven.compiler.source>` and `<maven.compiler.target>` to `21`.
  - Optionally update any plugin or dependency versions that require newer Java.
- Ensure your local `JAVA_HOME` points to JDK 21 and run `mvn -v` to confirm.
- Run `mvn clean test` and `mvn package` and fix any compilation issues.

**Large files / GitHub push issues**
This repository currently contains local JavaFX SDK binaries and native libs that are large. GitHub rejects files >100MB and large repos slow down pushes.

Fast fix (remove from index and ignore):

```bash
# remove from git index but keep local copies
git rm --cached openjfx-17.0.10_linux-x64_bin-sdk.zip
git rm --cached -r javafx-sdk-17.0.10/
git rm --cached -r openjfx-25.0.2_linux-x64_bin-sdk/
# add to .gitignore
echo "openjfx-*-linux-*_bin-sdk.zip" >> .gitignore
echo "javafx-sdk-*/" >> .gitignore
# commit and push
git add .gitignore
git commit -m "Remove local JavaFX SDKs and ignore them"
git push origin main
```

If those files were already committed historically, use a history-rewrite tool like `git filter-repo` or `bfg` to purge them from history before pushing. I can help run that if you want.

Alternative: Use Git LFS for large binaries (requires enabling LFS for the repo):

```bash
git lfs install
git lfs track "*.so"
git lfs track "openjfx-*.zip"
git add .gitattributes
# recommit large files as LFS
```

**Files modified**
- [pom.xml](pom.xml): added JUnit test dependencies and `maven-surefire-plugin` to run tests.
- Added tests under `src/test/java/` for `Category`, `User`, `Document`, and `DataManager`.

