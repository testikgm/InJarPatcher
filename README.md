# InJarPatcher 

Консольная Java-утилита для модификации байткода и внедрения собственного кода в уже скомпилированные `.jar` файлы (плагины, библиотеки, приложения) без необходимости декомпилировать и пересобирать весь проект.

## Основные возможности

1. **Интерактивный мастер (`--interactive`)**: сам читает JAR, выводит список классов и методов, позволяя выбрать цель по номеру в консоли.
2. **3 режима модификации**:
   - `REPLACE_BODY` — полная замена тела метода своим кодом.
   - `INSERT_BEFORE` — выполнение своего кода перед оригинальным методом (с доступом к аргументам через `$1`, `$2` и т.д.).
   - `INSERT_AFTER` — выполнение своего кода после завершения метода.
3. **Пакетный патчинг (`--config`)**: применение нескольких правил сразу через JSON-файл.
4. **Потоковая обработка**: не распаковывает весь архив на диск, сохраняя структуру, ресурсы и манифест JAR.

## Примеры использования

### 1. Интерактивный режим (самый простой)
Утилита сама покажет список классов и методов и предложит ввести код:
```bash
java -jar injar-patcher.jar -i plugin.jar --interactive
```

### 2. Быстрый просмотр всех классов в архиве
Если нужно просто посмотреть структуру классов внутри JAR:
```bash
java -jar injar-patcher.jar -i plugin.jar --list
```

### 3. Быстрый патч через аргументы командной строки

**Вставить хук перед началом метода (`INSERT_BEFORE`):**
```bash
java -jar injar-patcher.jar -i input.jar -o output.jar -c com.example.plugin.Main -m onEnable --mode INSERT_BEFORE -b "{ System.out.println(\"Plugin hooked!\"); }"
```

**Полностью заменить тело метода (`REPLACE_BODY`):**
```bash
java -jar injar-patcher.jar -i input.jar -o output.jar -c com.example.plugin.Calculator -m add -b "{ return 42; }"
```

### 4. Пакетный патч через JSON-конфиг
Подходит, если нужно изменить сразу несколько методов в разных классах за один проход:
```bash
java -jar injar-patcher.jar -i input.jar -o output.jar --config patch.json
```

**Пример `patch.json`:**
```json
{
  "rules": [
    {
      "targetClass": "com.example.plugin.Main",
      "targetMethod": "onEnable",
      "mode": "INSERT_BEFORE",
      "code": "{\n  getLogger().info(\"Plugin patched successfully!\");\n}"
    },
    {
      "targetClass": "com.example.plugin.LicenseChecker",
      "targetMethod": "isValid",
      "mode": "REPLACE_BODY",
      "code": "{\n  return true;\n}"
    }
  ]
}
```
