Инструкция «Как запустить Front и Back»
1.	Создайте и сохраните проект 
2.	Откройте MWPBack в IntelliJ IDEA, а MWPFront в WebStorm
3.	Далее в MWPBack откройте настройки конфигурации 
4.	Выберите Edit Configuration 
5.	Слева нажмите + и выберите Node js 
6.	Введите настройки: 
Working directory: C:\GitLab\Mobia\MobiaApp\Admin\Back
JS file: bin\www
Application parameters: dbreinit daoreinit
Environment variables:  DEBUG=back:*
Нажмите Apply и Ok (внизу справа)
7.	Нажмите debug
8.	Для MWPFront так же откройте настройки конфигурации и нажмите +
9.	Затем выберете Application и заполните поля:
	Name: Angular Application
	URL: http:\\localhost:4200
	Browser: Google
	Нажмите Apply и Ok (внизу справа)
10.	Откройте терминал проекта и вставьте ng serve –proxy-config proxy.conf.json
	Нажмите Enter
11.	Нажмите debug (IDE WebStorm сама откроет окно гугл с веб-приложением)


MWP
M - Manager
W - Work
P - Place