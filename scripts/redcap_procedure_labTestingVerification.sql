DROP PROCEDURE IF EXISTS labTestingVerification;
DELIMITER //

-- ----------------------------------------------------------------------
-- Procedure for responding to the receiving form
-- ----------------------------------------------------------------------
CREATE PROCEDURE labTestingVerification(
	IN project_id int(10),
	IN event_id int(10),
	IN record varchar(100),
	IN field_name varchar(100),
	IN i_value text,
	IN i_instance smallint(4)
)
BEGIN
	declare lab_report smallint(4);
	declare lab_receiving smallint(4);
	declare lab_extraction smallint(4);
	declare report_max_event smallint(4);
	declare test_verify_datetime varchar(255);
	declare test_verify_personnel varchar(255);
	declare test_verify_batch_id varchar(255);
	declare pos varchar(2);
	declare results varchar(2);
	declare rna_results varchar(2);
	declare verified varchar(100);
	
	declare c_project_id int(10);
	declare c_event_id int(10);
	declare c_record varchar(100);
	declare c_field_name varchar(100);
	declare c_value text;
	declare c_instance smallint(4);
    
	declare specimen cursor
	for
	select project_id, event_id, record, field_name, value, instance
	from redcap_data
	where project_id = project_id
		and event_id = event_id
		and record = record
		and field_name like 'test_det_barcode_%';
	-- Stop calling this procedure with any other data
	if (field_name = 'verification_complete' and i_value = '2' and project_id = 345) then
		
		select 352 into lab_report;	
		select 362 into lab_receiving;	
		select 344 into lab_extraction;	
		select 345 into lab_results;
		
		select max(event_id) into report_max_event 
		from redcap_data 
		where project_id = lab_report;
								
		select value into test_verify_datetime
		from redcap_data 
		where project_id = project_id 
			and event_id = event_id 
			and record = record 
			and field_name = 'test_verify_datetime';
			
		select value into test_verify_personnel
		from redcap_data 
		where project_id = project_id 
			and event_id = event_id 
			and record = record 
			and field_name = 'test_verify_personnel';
								
		select value into test_verify_batch_id
		from redcap_data 
		where project_id = project_id 
			and event_id = event_id 
			and record = record 
			and field_name = 'test_verify_batch_id';
		
		-- ------------------------------------------------------------------
		-- Go through the specimen in the batch by looking for the 
		-- barcodes.
		-- ------------------------------------------------------------------
		open specimen;
		s_loop: LOOP
			fetch specimen into c_project_id, c_event_id, c_record, c_field_name, c_value, c_instance;	
			select substring(c_field_name, 20, 2) into pos;
			
			-- Get the results of the specimen
			select value into results
			from redcap_data 
			where project_id = c_project_id 
				and event_id = c_event_id 
				and record = c_record 
				and field_name = concat('test_verify_result_', pos);
							
			select value into rna_results
			from redcap_data 
			where project_id = project_id 
				and event_id = event_id 
				and record = record 
				and field_name = concat('covid_rna_results', pos);
				
			select value into verified
			from redcap_data
			where project_id = lab_report 
				and event_id = report_max_event 
				and record = c_value 
				and field_name = 'test_verify_result';
			
			-- If this was saved before, we update
			if verfied is not null then
				update redcap_data
				set value = results
				where project_id = c_project_id 
					and event_id = c_event_id 
					and record = c_record 
					and field_name = 'test_verify_result';
				
				update redcap_data
				set value = test_verify_batch_id
				where project_id = c_project_id 
					and event_id = c_event_id 
					and record = c_record 
					and field_name = 'test_verify_batch_id';
					
				update redcap_data
				set value = test_verify_personnel
				where project_id = c_project_id 
					and event_id = c_event_id 
					and record = c_record 
					and field_name = 'test_verify_personnel';
				
				update redcap_data
				set value = test_verify_datetime
				where project_id = c_project_id 
					and event_id = c_event_id 
					and record = c_record 
					and field_name = 'test_verify_datetime';
					
				update redcap_data
				set value = rna_results
				where project_id = c_project_id 
					and event_id = c_event_id 
					and record = c_record 
					and field_name = 'covid_rna_results';
			else
				
				-- insert condition into the lab report
				insert into redcap_data (project_id, event_id, record, field_name, value)
				values (lab_report, report_max_event, c_value, 'test_verify_result', results),
						(lab_report, report_max_event, c_value, 'covid_rna_results', rna_results),
						(lab_report, report_max_event, c_value, 'test_verify_batch_id', test_verify_batch_id),
						(lab_report, report_max_event, c_value, 'test_verify_personnel', test_verify_personnel),
						(lab_report, report_max_event, c_value, 'test_verify_datetime', test_verify_datetime);
			end if;
				
			if (results = '5') then
				-- Update the specimen in the staging area
				update redcap_ddp_specimen 
				set results = rna_results,
					results_verified_by = test_verify_personnel,
					results_verified_date = test_verify_datetime,
					dhis2_synched = false
				where specimen_barcode = c_value;
							
			end if;
		END LOOP s_loop;
		
	end if;

END //

DELIMITER ;

