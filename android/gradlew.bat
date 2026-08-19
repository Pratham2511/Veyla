@rem
@rem Copyright © 2015-2024 the original authors.
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
@rem Placeholder script — generate the real wrapper by running:
@rem   gradle wrapper --gradle-version 8.11.1
@rem
@rem ##############################################################################

@echo off
set DEFAULT_APP_HOME=%~dp0
set APP_HOME=%DEFAULT_APP_HOME:~0,-1%
set WRAPPER_JAR=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

if not exist "%WRAPPER_JAR%" (
    echo Gradle wrapper JAR not found. Please generate it by running:
    echo   gradle wrapper --gradle-version 8.11.1
    exit /b 1
)

rem Delegate to the real Gradle wrapper
"%WRAPPER_JAR%" %*
