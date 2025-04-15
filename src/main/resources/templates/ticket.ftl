<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="ru">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <style type="text/css">
        @font-face {
            font-family: "TimesNewRoman";
            src: url("classpath:/fonts/TimesNewRoman.ttf") format("truetype");
        }
        body   { font-family: "TimesNewRoman", sans-serif; font-size: 12pt; }
        h1     { text-align: center; margin: 0 0 14pt 0; }
        table  { width: 100%; border-collapse: collapse; margin-top: 18pt; }
        td     { padding: 4pt 2pt; vertical-align: top; }
        .label { width: 38%; font-weight: bold; }
    </style>
</head>
<body>

<h1>Заявление о&nbsp;вступлении&nbsp;в&nbsp;профсоюз СППО&nbsp;РУТ&nbsp;(МИИТ)</h1>

<p>Прошу принять меня в члены профсоюза СППО&nbsp;РУТ&nbsp;(МИИТ).</p>

<table>
    <tr>
        <td class="label">Фамилия, имя, отчество</td>
        <td>
            ${form.lastName()} ${form.firstName()} ${form.middleName()?default('')}
        </td>
    </tr>
    <tr>
        <td class="label">Дата рождения</td>
        <td>
            ${form.birthDate()?default('')}
        </td>
    </tr>
    <tr>
        <td class="label">Группа / должность</td>
        <td>${form.groupOrPosition()?default('')}</td>
    </tr>
    <tr>
        <td class="label">Телефон</td>
        <td>${form.phone()?default('')}</td>
    </tr>
    <tr>
        <td class="label">E-mail</td>
        <td>${form.email()?default('')}</td>
    </tr>
</table>

<p style="margin-top:36pt;">Дата&nbsp;подачи: ${now}</p>

<p style="text-align:right; margin-top:60pt;">_____________/ _________________ /</p>

</body>
</html>
