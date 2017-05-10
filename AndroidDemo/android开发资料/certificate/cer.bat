set keystore=androidEngine.keystore
set file=androidEngine.cer

keytool -export -keystore %keystore% -alias androidEngine -file %file% -rfc

:: -export指定为导出操作
:: -keystore指定keystore文件
:: -alias指定导出keystore文件中的别名
:: -file指向导出路径
:: -rfc以文本格式输出，也就是以BASE64编码输出

pause