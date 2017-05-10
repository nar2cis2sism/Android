set keystore=androidEngine.keystore

keytool -genkey -validity 36000 -alias androidEngine -keyalg RSA -keystore %keystore%

:: -genkey表示生成密钥
:: -validity指定证书有效期，这里是36000天
:: -alias指定别名，这里是androidEngine
:: -keyalg指定算法，这里是RSA
:: -keystore指定存储位置

pause