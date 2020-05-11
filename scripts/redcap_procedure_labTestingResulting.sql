DROP PROCEDURE IF EXISTS labTestingResulting;
DELIMITER //

-- ----------------------------------------------------------------------
-- Procedure for responding to the receiving form
-- ----------------------------------------------------------------------
CREATE PROCEDURE labTestingResulting(
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
	declare lab_results smallint(4);
	declare report_max_event smallint(4);
	declare test_assay_datetime varchar(255);
	declare test_assay_personnel varchar(255);
	declare test_assay_instrument varchar(255);
	declare test_assay_batchsize varchar(255);
	declare test_assay_batch_id varchar(255);
	declare pos varchar(2);
	declare results varchar(2);
	
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
	if (field_name = 'testing_detection_complete' and i_value = '2' and project_id = 345) then
		
		select 352 into lab_report;	
		select 362 into lab_receiving;	
		select 344 into lab_extraction;	
		select 345 into lab_results;
		
		select max(event_id) into report_max_event from redcap_data where project_id = lab_report;
					
		select value into test_assay_datetime
		from redcap_data 
		where project_id = project_id 
			and event_id = event_id 
			and record = record 
			and field_name = 'test_assay_datetime';
			
		select value into test_assay_personnel
		from redcap_data 
		where project_id = project_id 
			and event_id = event_id 
			and record = record 
			and field_name = 'test_assay_personnel';
			
		select value into test_assay_instrument
		from redcap_data 
		where project_id = project_id 
			and event_id = event_id 
			and record = record 
			and field_name = 'test_assay_instrument';
			
		select value into test_assay_batchsize
		from redcap_data 
		where project_id = project_id 
			and event_id = event_id 
			and record = record 
			and field_name = 'test_assay_batchsize';
						
		select value into test_assay_batch_id
		from redcap_data 
		where project_id = project_id 
			and event_id = event_id 
			and record = record 
			and field_name = 'test_assay_batch_id';
		
		-- ------------------------------------------------------------------
		-- Go through the specimen in the batch by looking for the 
		-- barcodes.
		-- ------------------------------------------------------------------	
			
		open specimen;
		s_loop: LOOP
			fetch specimen into c_project_id, c_event_id, c_record, c_field_name, c_value, c_instance;	
			select substring(c_field_name, 17, 2) into pos;
			
			-- Get the results of the specimen
			select value into results
			from redcap_data 
			where project_id = c_project_id 
				and event_id = c_event_id 
				and record = c_record 
				and field_name = concat('test_assay_result_', pos);
			
			-- Update the specimen in the staging area
			update redcap_ddp_specimen 
			set restults = results,
				results_entered_by = test_assay_personnel,
				results_entered_date = test_assay_datetime
			where specimen_barcode = c_value;
						
			-- insert condition into the lab report
			insert into redcap_data (project_id, event_id, record, field_name, value)
			values (lab_report, report_max_event, c_value, 'test_assay_id', pos),
					(lab_report, report_max_event, c_value, 'test_assay_result', results),
					(lab_report, report_max_event, c_value, 'test_assay_batch_id', test_assay_batch_id),
					(lab_report, report_max_event, c_value, 'test_assay_batchsize', test_assay_batchsize),
					(lab_report, report_max_event, c_value, 'test_assay_datetime', test_assay_datetime),
					(lab_report, report_max_event, c_value, 'test_assay_instrument', test_assay_instrument),
					(lab_report, report_max_event, c_value, 'test_assay_personnel', test_assay_personnel),
					(lab_report, report_max_event, c_value, 'test_assay_instrument', test_assay_instrument);
		END LOOP s_loop;
		
	end if;

END //

DELIMITER ;

