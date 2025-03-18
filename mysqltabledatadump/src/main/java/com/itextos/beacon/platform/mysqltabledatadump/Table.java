package com.itextos.beacon.platform.mysqltabledatadump;

import java.util.HashSet;
import java.util.Set;

public class Table {

	public static Set<String> ACCOUNTS=new HashSet<String>();
	public static Set<String> CARRIER_HANDOVER=new HashSet<String>();
	public static Set<String> CLIENT_HANDOVER=new HashSet<String>();
	public static Set<String> CM=new HashSet<String>();
	public static Set<String> CONFIGURATION=new HashSet<String>();

	public static Set<String> IMP=new HashSet<String>();
	public static Set<String> LISTING=new HashSet<String>();
	public static Set<String> LOGGING=new HashSet<String>();
	public static Set<String> MESSAGING=new HashSet<String>();
	public static Set<String> PAYLOAD=new HashSet<String>();
	
	public static Set<String> R3C=new HashSet<String>();
	public static Set<String> SYSCONFIG=new HashSet<String>();
	
	static {
		loadAccount();
		loadCarrierHandover();
		loadClientHandover();
		loadCM();
		loadConfiguration();
		loadIMP();
		loadLISTING();
		loadLOGGING();
		loadMessaging();
		loadPayload();
		loadR3C();
		loadSysconfig();
	}

	private static void loadCarrierHandover() {
		
		CARRIER_HANDOVER.add("carrier_master");
		
		CARRIER_HANDOVER.add("carrier_onnet_table_map");
		CARRIER_HANDOVER.add("carrier_premium_routes");
		CARRIER_HANDOVER.add("carrier_route_map");
		CARRIER_HANDOVER.add("client_dn_gen_percentage_map");
		CARRIER_HANDOVER.add("client_msg_validity");
		CARRIER_HANDOVER.add("client_retry_validity");
		CARRIER_HANDOVER.add("client_route_config");
		CARRIER_HANDOVER.add("common_retry_route_config");
		CARRIER_HANDOVER.add("common_retry_validity");
		CARRIER_HANDOVER.add("dn_gen_percentage_exempt");
		CARRIER_HANDOVER.add("dn_gen_percentage_map");
		CARRIER_HANDOVER.add("govt_header");
		CARRIER_HANDOVER.add("govt_header_exclude");
		CARRIER_HANDOVER.add("govt_header_masking");

		
		CARRIER_HANDOVER.add("group_routes_ratio");
		CARRIER_HANDOVER.add("header_alternate_routes");
		CARRIER_HANDOVER.add("header_fixed_routes");
		CARRIER_HANDOVER.add("header_mask_pool");
		CARRIER_HANDOVER.add("header_priority_open_routes");
		CARRIER_HANDOVER.add("header_random_pool");
		CARRIER_HANDOVER.add("header_route_status");
		CARRIER_HANDOVER.add("intl_carrier_support_headers");
		CARRIER_HANDOVER.add("intl_client_header_template");

		CARRIER_HANDOVER.add("intl_client_route_info");
		CARRIER_HANDOVER.add("intl_country_header_info");
		CARRIER_HANDOVER.add("intl_country_header_template");
		CARRIER_HANDOVER.add("intl_global_header_template");
		CARRIER_HANDOVER.add("intl_mobile_routes");
		CARRIER_HANDOVER.add("intl_route_config");
		CARRIER_HANDOVER.add("intl_route_header");
		CARRIER_HANDOVER.add("kannel_url_config");
		CARRIER_HANDOVER.add("mcc_mnc");
		CARRIER_HANDOVER.add("mcc_mnc_routes");
		CARRIER_HANDOVER.add("mccmnclist");
		CARRIER_HANDOVER.add("mobile_route_config");

		
		
		CARRIER_HANDOVER.add("msg_replace_keywords");
		CARRIER_HANDOVER.add("msg_replace_route_condition");
		CARRIER_HANDOVER.add("msg_validity");
		CARRIER_HANDOVER.add("otp_voice_connect_map");
		CARRIER_HANDOVER.add("otp_voice_template_map");
		CARRIER_HANDOVER.add("priority_routes");
		CARRIER_HANDOVER.add("rerouting_config");
		CARRIER_HANDOVER.add("retry_config");
		CARRIER_HANDOVER.add("retry_route_config");

		
		CARRIER_HANDOVER.add("route_configuration");
		CARRIER_HANDOVER.add("route_group_config");
		CARRIER_HANDOVER.add("route_headers_pool");
		CARRIER_HANDOVER.add("template_based_routing");

	}

	private static void loadSysconfig() {
	
		SYSCONFIG.add("jndi_info");
	}

	private static void loadR3C() {

		R3C.add("r3c_0");
		R3C.add("r3c_1");
		R3C.add("r3c_10");
		R3C.add("r3c_11");
		R3C.add("r3c_12");

		R3C.add("r3c_13");
		R3C.add("r3c_14");
		R3C.add("r3c_15");
		R3C.add("r3c_16");
		R3C.add("r3c_17");

		R3C.add("r3c_18");
		R3C.add("r3c_19");
		R3C.add("r3c_2");
		R3C.add("r3c_20");
		R3C.add("r3c_3");

		R3C.add("r3c_4");
		R3C.add("r3c_5");
		R3C.add("r3c_6");
		R3C.add("r3c_7");
		R3C.add("r3c_8");

		R3C.add("r3c_9");
		R3C.add("r3c_exclude_url");
		R3C.add("r3c_include_url");
		R3C.add("r3c_smartlink_info");
		

	}

	private static void loadPayload() {
		// TODO Auto-generated method stub
		
	}

	private static void loadMessaging() {
		// TODO Auto-generated method stub
		
	}

	private static void loadLOGGING() {
		// TODO Auto-generated method stub
		
	}

	private static void loadLISTING() {
		
			LISTING.add("blacklist$");
			LISTING.add("block_list_headers");
			LISTING.add("block_list_numbers");
			LISTING.add("block_list_spam_words");
			LISTING.add("block_list_spam_words_msgtype");
			LISTING.add("client_block_list_numbers");
	//		LISTING.add("dnd_data");
			LISTING.add("exceptions_spam_block_list");
			LISTING.add("interface_sms_template");
			
			
			LISTING.add(" intl_block_list_spam_words");
			LISTING.add("intl_block_list_spam_words_msgtype");
			LISTING.add("intl_exceptions_spam_block_list");
			LISTING.add("optout_list");
			LISTING.add("whitelist_mobiles");

	}

	private static void loadIMP() {
		
		IMP.add("management_user");
	}

	private static void loadConfiguration() {
		
		CONFIGURATION.add("app_config_values");
		CONFIGURATION.add("bill_log_map");
		CONFIGURATION.add("bill_log_map_default");
		CONFIGURATION.add("billing_currency_master");
		CONFIGURATION.add("calendar_info");
		CONFIGURATION.add("carrier_error_code");
		CONFIGURATION.add("circle_exclude_config");
		CONFIGURATION.add("client_crypto_info");
		CONFIGURATION.add("client_error_code");
		CONFIGURATION.add("client_intl_credits");

		
		
		CONFIGURATION.add("client_intl_rates");
		CONFIGURATION.add("client_msg_prefix_suffix");
		CONFIGURATION.add("client_specific_component");
		CONFIGURATION.add("client_throttle");
		CONFIGURATION.add("cluster_component_datasource_map_detail");
		CONFIGURATION.add("cluster_component_datasource_map_detail_org");
		CONFIGURATION.add("cluster_component_datasource_map_master");
		CONFIGURATION.add("cluster_component_datasource_map_master_org");
		CONFIGURATION.add("cluster_type");
		CONFIGURATION.add("component");


		
		CONFIGURATION.add("country_info");
		CONFIGURATION.add("currency_rates_daily");
		CONFIGURATION.add("currency_rates_date_history");
		CONFIGURATION.add("currency_rates_monthly");
		CONFIGURATION.add("data_refresher");
		CONFIGURATION.add("dn_adjustment_child");
		CONFIGURATION.add("dn_adjustment_parent");
		CONFIGURATION.add("dn_receiver_info");
		CONFIGURATION.add("dn_receiver_master");
		CONFIGURATION.add("error_code_mapping");


		
		CONFIGURATION.add("es_sub_del_t2_col_map");
		CONFIGURATION.add("fmsg_log_download_col_map");
		CONFIGURATION.add("inmemory_loader_config");
		CONFIGURATION.add("interface_group_master");
		CONFIGURATION.add("interface_master");
		CONFIGURATION.add("interface_parameter_config");
		CONFIGURATION.add("interface_parameter_customer_key");
		CONFIGURATION.add("interface_parameter_key_list");
		CONFIGURATION.add("interface_parameter_master");
		CONFIGURATION.add("intl_credits");


		
		
		CONFIGURATION.add("intl_rates");
		CONFIGURATION.add("jndi_info");
		CONFIGURATION.add("kafka_cluster");
		CONFIGURATION.add("kafka_component");
		CONFIGURATION.add("kafka_consumer_group");
		CONFIGURATION.add("kafka_topic");
		CONFIGURATION.add("log_download_col_map");
		CONFIGURATION.add("mcc_mnc_rates");
		CONFIGURATION.add("mdb_to_pg_log_col_map");
		CONFIGURATION.add("msc_code_map");


		
		
		CONFIGURATION.add("pg_bill_log_map");
		CONFIGURATION.add("pg_bill_log_map_default");
		CONFIGURATION.add("platform_cluster_component_kafka_cluster_map");
		CONFIGURATION.add("platform_cluster_kafka_topic_map");
		CONFIGURATION.add("platform_cluster_kafka_topic_map_org");
		CONFIGURATION.add("query_async_queue");
		CONFIGURATION.add("query_async_queue_exec_log");
		CONFIGURATION.add("query_async_queue_req_info");
		CONFIGURATION.add("redis_config");
		CONFIGURATION.add("redis_config_org");


		
		
		CONFIGURATION.add("sales_master");
		CONFIGURATION.add("topic2table_config");
		CONFIGURATION.add("zz_pg_sub_del_columns");
	}

	private static void loadCM() {
		
		CM.add("billing_rate_changes");
		CM.add("campaign_file_splits");
		CM.add("campaign_files");
		CM.add("campaign_groups");
		CM.add("campaign_master");
		CM.add("campaign_schedule_at");
		CM.add("campaign_schedule_master");
		CM.add("cm_features_blacklist");
		
		
		CM.add("config_params");
		CM.add("config_user_fullmessage");
		CM.add("config_user_quicklinks");
		CM.add("config_user_whitelabel");
		CM.add("contactus_request");
		CM.add("dlt_template_column_mapping");
		CM.add("dlt_template_files");
		CM.add("dlt_template_invalid_rows");
		
		
		
		CM.add("dlt_template_request");
		CM.add("download_req");
		CM.add("group_file_splits");
		CM.add("group_files");
		CM.add("group_master");
		CM.add("group_user_mapping");
		
		
		CM.add("queue_routing");
		CM.add("redis_info_cm");
		CM.add("sms_priority");
		CM.add("template_master");

		CM.add("timezone");
		CM.add("two_factor_authentication");

		CM.add("user_configs");

		CM.add("wallet_transactions");


		
	}

	private static void loadClientHandover() {
		
		CLIENT_HANDOVER.add("client_handover_config_body_params");
		CLIENT_HANDOVER.add("client_handover_config_detail");
		CLIENT_HANDOVER.add("client_handover_config_header_params");
		CLIENT_HANDOVER.add("client_handover_config_master");
		
		CLIENT_HANDOVER.add("dlrquery_config_body_params");
		CLIENT_HANDOVER.add("dlrquery_config_detail");
		CLIENT_HANDOVER.add("dlrquery_config_header_params");
		CLIENT_HANDOVER.add("dlrquery_config_master");
		CLIENT_HANDOVER.add("dlrquery_response_master");
	}

	private static void loadAccount() {

		ACCOUNTS.add("api_custom_response");
		ACCOUNTS.add("api_custom_response_error_code_mapping");
		ACCOUNTS.add("client_dlr_query_config");
		ACCOUNTS.add("client_dntype_config");
		ACCOUNTS.add("client_handover_config_master");
		ACCOUNTS.add("custom_features");
		ACCOUNTS.add("data_event_log");
		ACCOUNTS.add("dlt_template_group");
		ACCOUNTS.add("dlt_template_group_header_entity_map");
		ACCOUNTS.add("dlt_template_info");
		ACCOUNTS.add("dlt_template_prefix_suffix");
		ACCOUNTS.add("feature_desc");
		ACCOUNTS.add("no_payload_dn_retry");
		ACCOUNTS.add("prepaid_recharge");
		ACCOUNTS.add("service");
		ACCOUNTS.add("su_config");
		ACCOUNTS.add("sub_service");
		ACCOUNTS.add("user_config");
		ACCOUNTS.add("user_headers_intl");
		ACCOUNTS.add("user_service_map");
		ACCOUNTS.add("user_smpp_config");
		ACCOUNTS.add("users_templategroup_ids");

	}

	
}
