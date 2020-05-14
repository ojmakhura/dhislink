DROP TRIGGER IF EXISTS `lab_report_update_trigger`;

DELIMITER $$
 
create trigger lab_report_update_trigger
after update
on redcap_data for each row
begin

	declare lab_report smallint(4);    
    
	declare c_project_id int(10);
	declare c_event_id int(10);
	declare c_record varchar(100);
	declare c_field_name varchar(100);
	declare c_value text;
	declare c_instance smallint(4);
    
	select 352 into lab_report;

	-- Stop calling this procedure with any other data
	if (new.field_name = 'covid19_lab_report_complete' and new.value = '2' and new.project_id = lab_report) then
	
		select value into c_value
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'covid_rna_results';
		
		-- update the staging
		update redcap_ddp_specimen
		set results = c_value,
			dhis2_synched = false
		where specimen_barcode = new.record;
	
	elseif (new.field_name = 'covid_rna_results' and new.project_id = lab_report) then
        
        select value into c_value
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'covid19_lab_report_complete';
					
		if (c_value = '2') then
			-- update the staging
			update redcap_ddp_specimen
			set dhis2_synched = false,
				results = new.value
			where specimen_barcode = new.record;
		end if;
        
	end if;
end; $$

DELIMITER ;

DROP TRIGGER IF EXISTS `lab_report_insert_trigger`;

DELIMITER $$
 
create trigger lab_report_insert_trigger
after update
on redcap_data for each row
begin

	declare lab_report smallint(4);
	declare c_value text;
	declare test_verify_result varchar(4);    
	select 352 into lab_report;

	-- Stop calling this procedure with any other data
	if (new.field_name = 'covid19_lab_report_complete' and new.value = '2' and new.project_id = lab_report) then
	
		select value into test_verify_result
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'result_authorised';
		
		if (test_verify_result = '1') then
			-- update the staging
			update redcap_ddp_specimen
			set dhis2_synched = false
			where specimen_barcode = new.record;
		end if;
		
	elseif (new.field_name = 'result_authorised' and new.value = '1' and new.project_id = lab_report) then
	
		select value into c_value
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'covid_rna_results';
		
		-- update the staging
		update redcap_ddp_specimen
		set dhis2_synched = false
		where specimen_barcode = new.record;
		
	elseif (new.field_name = 'covid_rna_results' and new.project_id = lab_report) then
        			
		-- update the staging
		update redcap_ddp_specimen
		set results = new.value
		where specimen_barcode = new.record;
		
	elseif (new.field_name = 'authorizer_datetime' and new.project_id = lab_report) then
		
		-- update the staging
		update redcap_ddp_specimen
		set results_authorised_date = new.value
		where specimen_barcode = new.record;
		
	elseif (new.field_name = 'authorizer_personnel' and new.project_id = lab_report) then
	
		-- update the staging
		update redcap_ddp_specimen
		set results_authorised_by = new.value
		where specimen_barcode = new.record;
	end if;
		
end; $$

DELIMITER ;
