DROP PROCEDURE IF EXISTS labTestingDetection;
DELIMITER //

-- ----------------------------------------------------------------------
-- Procedure for responding to the receiving form
-- ----------------------------------------------------------------------
CREATE PROCEDURE labTestingDetection(
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
	declare detection_lab varchar(255);
	declare test_det_datetime varchar(255);
	declare test_det_personnel varchar(255);
	declare test_det_instrument varchar(255);
	declare test_det_batchsize varchar(255);
	declare test_det_batch_id varchar(255);
	declare test_det_barcode varchar(255);
	declare pos varchar(2);
	
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
		
		select value into detection_lab
		from redcap_data 
		where project_id = project_id 
			and event_id = event_id 
			and record = record 
			and field_name = 'detection_lab';
			
		select value into test_det_datetime
		from redcap_data 
		where project_id = project_id 
			and event_id = event_id 
			and record = record 
			and field_name = 'test_det_datetime';
			
		select value into test_det_personnel
		from redcap_data 
		where project_id = project_id 
			and event_id = event_id 
			and record = record 
			and field_name = 'test_det_personnel';
			
		select value into test_det_instrument
		from redcap_data 
		where project_id = project_id 
			and event_id = event_id 
			and record = record 
			and field_name = 'test_det_instrument';
			
		select value into test_det_batchsize
		from redcap_data 
		where project_id = project_id 
			and event_id = event_id 
			and record = record 
			and field_name = 'test_det_batchsize';
						
		select value into test_det_batch_id
		from redcap_data 
		where project_id = project_id 
			and event_id = event_id 
			and record = record 
			and field_name = 'test_det_batch_id';
		
		-- ------------------------------------------------------------------
		-- Go through the specimen in the batch by looking for the 
		-- barcodes.
		-- ------------------------------------------------------------------	
			
		open specimen;
		s_loop: LOOP
			fetch specimen into c_project_id, c_event_id, c_record, c_field_name, c_value, c_instance;	
			select substring(c_field_name, 18, 2) into pos;
			
			-- insert condition into the lab report
			insert into redcap_data (project_id, event_id, record, field_name, value)
			values (lab_report, report_max_event, c_value, 'test_det_barcode', c_value),
					(lab_report, report_max_event, c_value, 'test_det_id', pos),
					(lab_report, report_max_event, c_value, 'detection_lab', extraction_lab),
					(lab_report, report_max_event, c_value, 'test_det_batch_id', test_det_batch_id),
					(lab_report, report_max_event, c_value, 'test_det_batchsize', test_det_batchsize),
					(lab_report, report_max_event, c_value, 'test_det_datetime', test_det_datetime),
					(lab_report, report_max_event, c_value, 'test_det_instrument', test_det_instrument),
					(lab_report, report_max_event, c_value, 'test_det_personnel', test_det_personnel),
					(lab_report, report_max_event, c_value, 'test_det_instrument', test_det_instrument);
		END LOOP s_loop;
		
	end if;

END //

DELIMITER ;

