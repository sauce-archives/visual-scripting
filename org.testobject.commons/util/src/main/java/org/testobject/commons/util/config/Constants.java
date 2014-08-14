package org.testobject.commons.util.config;

public interface Constants {

	String runtime_debug_mode = "runtime.debug.mode";
	String mail_service_class = "mail.service.class";
	String javax_mail_user = "javax.mail.user";
	String javax_mail_password = "javax.mail.password";
	String javax_mail_system_from = "javax.mail.system.from";
	String javax_mail_system_to = "javax.mail.system.to";

	String system_name = "testobject.system.name";
	String instashot_screenshot_mock = "testobject.instashot.screenshot.mock";
	String instashot_screenshot_wait = "testobject.instashot.screenshot.wait.seconds";

	String dynamodb_client_class = "dynamodb.client.class";
	String dynamodb_client_endpoint = "dynamodb.client.endpoint";
	String dynamodb_client_access_key = "dynamodb.client.access.key";
	String dynamodb_client_secret_key = "dynamodb.client.secret.key";

	String s3_region = "s3.region";
	String s3_test_bucket_prefix = "s3.test.bucket.prefix";

	String db_prefix = "db.prefix";
	String db_mapper_class = "db.mapper.class";
	String file_mapper_class = "file.mapper.class";
	String app_file_mapper_class = "app.file.mapper.class";

	String android_sdk_location = "android.sdk.location";
	String android_dev_mode = "android.dev.mode";

	String application_work_folder = "application.work.folder";
	String font_folder = "font.folder";

	String env_host_name = "env.host.name";
	String env_http_endpoint_ssl = "env.http.endpoint.ssl";

	String console_ports_start = "console.ports.start";
	String console_ports_end = "console.ports.end";

	String thread_count_device_pool = "threadcount.pool";
	String thread_count_instashot_executor = "threadcount.instashot.executor";

	String paymill_api_key = "paymill.api.key";

	String iron_io_token = "iron.io.token";
	String iron_io_project_id = "iron.io.project.id";

	String github_app_client_id = "github.app.client.id";
	String github_app_client_secret = "github.app.client.secret";

	String device_type = "device.type";

	String device_pool_jar = "device.pool.jar";
	String device_pool_libs = "device.pool.libs";
	String device_pool_logback = "device.pool.logback";
	String device_pool_jetty_port_begin = "device.pool.jetty.port.begin";
	String device_pool_jetty_port_end = "device.pool.jetty.port.end";
	String device_pool_genymotion_enabled = "device.pool.genymotion.enabled";
	String device_pool_emulators_enabled = "device.pool.emulators.enabled";
	String device_pool_real_devices_enabled = "device.pool.real.devices.enabled";
	String device_pool_max_running_devices = "device.pool.max.running.devices";
	String device_pool_adb_bridge_port_start = "device.pool.adb.bridge.start";
	String device_pool_adb_bridge_port_end = "device.pool.adb.bridge.end";
	String device_pool_vnc_port_start = "device.pool.vnc.port.start";
	String device_pool_vnc_port_end = "device.pool.vnc.port.end";
	String device_pool_port_offset = "device.pool.vnc.port.offset";
	String device_pool_usb_sym_linker = "device.pool.usb.sym.linker";
	String device_pool_reboot_devices = "device.pool.reboot.devices";
	String device_pool_delete_accounts = "device.pool.delete.accounts";
	String device_pool_reset_launcher = "device.pool.reset.launcher";
	String device_pool_vmlite_license = "device.pool.vmlite.license";

	String use_single_jvm = "use.single.jvm";
	String genymotion_location = "genymotion.location";

	String server_id = "server.id";
	String web_base_url = "web.baseurl";
	String web_app_rest= "web.app.rest";

	String capsule_api_token = "capsule.api.token";
	String capsule_api_domain = "capsule.api.domain";;
	String capsule_api_ms_suspect = "capsule.api.ms.suspect";
	String capsule_api_ms_prospect = "capsule.api.ms.prospect";
	String capsule_api_ms_track_online = "capsule.api.ms.trackOnline";
	String capsule_api_ms_lost = "capsule.api.ms.lost";
	String capsule_api_user_de = "capsule.api.user.de";
	String capsule_api_user_en = "capsule.api.user.en";
	String capsule_api_user_agent = "capsule.api.user.agent";
	String capsule_dropbox_email = "capsule.dropbox.email";

	String mailchimp_api_key = "mailchimp.api.key";
	String mailchimp_list_id = "mailchimp.list.id";

	String mandrill_api_key = "mandrill.api.key";

	String mondo_db = "mongo.db";
	String mongo_user = "mongo.user";
	String mongo_password = "mongo.password";
	String mongo_hostname = "mongo.hostname";
	String mongo_port = "mongo.port";

	String fastbill_user = "fastbill.user";
	String fastbill_token = "fastbill.token";

	String fastbill_queue = "fastbill.queue";

	String fastbill_iron_token = "fastbill.iron.token";
	String fastbill_iron_project = "fastbill.iron.project";
	String fastbill_iron_polling = "fastbill.iron.polling";
	String fastbill_iron_webhook_token = "fastbill.iron.webhook.token";

	String vnc_host = "vnc.host";
	String vnc_port = "vnc.port";

	String headless_replay_enabled = "headless.replay.enabled";
	String headless_replay_total_batch_threads = "headless.replay.batch.threads";
	String headless_replay_total_test_threads = "headless.replay.test.threads";
	String headless_replay_iron_project = "headless.replay.iron.project";
	String headless_replay_iron_token = "headless.replay.iron.token";

	String default_device = "default.device";

	String mixpanel_token = "mixpanel.token";

	String apk_parser_drawable_folders = "apk.parser.drawable.folders";
	String jminix_enabled = "jminix.enabled";

	String devices_google_account_password = "devices.google.account.password";

	String monitoring_auto_monitor_real_devices = "monitoring.auto.monitor.real.devices";
	String monitoring_auto_monitor_emulators = "monitoring.auto.monitor.emulators";
	String monitoring_autofix_real_devices = "monitoring.auto.fix.real.devices";
	
	String monitoring_thresholds_queue_warning = "monitoring.thresholds.queue.warning";
	String monitoring_thresholds_queue_critical = "monitoring.thresholds.queue.critical";

	String monitoring_thresholds_devices_warning = "monitoring.thresholds.devices.warning";
	String monitoring_thresholds_devices_critical = "monitoring.thresholds.devices.critical";

	String monitoring_thresholds_user_exceptions_warning = "monitoring.thresholds.user.exceptions.warning";
	String monitoring_thresholds_user_exceptions_critical = "monitoring.thresholds.user.exceptions.critical";	
	
}
