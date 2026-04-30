#!/usr/bin/env python3
import os
import zipfile
import urllib.request
import shutil

# Создаём обёртку вручную
wrapper_dir = "/root/projects/figura-fix/gradle/wrapper"
os.makedirs(wrapper_dir, exist_ok=True)

# Скачиваем JAR обёртки
wrapper_jar = os.path.join(wrapper_dir, "gradle-wrapper.jar")
gradle_zip_url = "https://services.gradle.org/distributions/gradle-8.15-bin.zip"

print(f"Creating gradle wrapper directory: {wrapper_dir}")

# Создаём gradlew script
gradlew = '''#!/usr/bin/env sh
gradle_home=$(cd "${0%{/}*}" && pwd)

CLASSPATH=$gradle_home/gradle/wrapper/gradle-wrapper.jar

exec java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
'''

with open("/root/projects/figura-fix/gradlew", "w") as f:
    f.write(gradlew)
os.chmod("/root/projects/figura-fix/gradlew", 0o755)

# Создаём gradle.bat для Windows
gradle_bat = '''@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

set JAVA_EXE=java.exe
%JAVA_EXE% -classpath "%APP_HOME%gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*

endlocal
'''

with open("/root/projects/figura-fix/gradlew.bat", "w") as f:
    f.write(gradle_bat)

print("Created gradlew and gradlew.bat")
