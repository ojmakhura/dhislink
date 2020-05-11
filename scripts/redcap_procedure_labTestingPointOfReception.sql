DROP PROCEDURE IF EXISTS labTestingPointOfReception;
DELIMITER //
-- ----------------------------------------------------------------------
-- Procedure for responding to a completion of the lab testing point
-- of reception.
-- ----------------------------------------------------------------------
CREATE PROCEDURE labTestingPointOfReception(
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
	declare batch_size varchar(100);
	declare report_max_event smallint(4);
	declare test_tpor_personnel varchar(100);
	declare test_tpor_batchsize varchar(100);
	declare test_tpor_datetime varchar(100);
	declare tpor_lab varchar(100);
	declare c_project_id int(10);
	declare c_event_id int(10);
	declare c_record varchar(100);
	declare c_field_name varchar(100);
	declare c_value text;
	declare c_instance smallint(4);
	declare pos varchar(2);
	
	declare specimen cursor
	for
	select project_id, event_id, record, field_name, value, instance
	from redcap_data
	where project_id = project_id
		and event_id = event_id
		and record = record
		and field_name like 'test_tpor_barcode_%';
		
	-- Stop calling this procedure with any other data
	if (field_name = 'testing_reception_complete' and i_value = '2' and project_id = 362) then
		select 352 into lab_report;
		select 362 into lab_receiving;		
		select 344 into lab_extraction;		
		select 345 into lab_results;

		select value into batch_size from redcap_data where record = new.record and field_name = 'lab_rec_batchsize';
		
		select max(event_id) into report_max_event from redcap_data where project_id = lab_report;
		
		select value into test_tpor_personnel
		from redcap_data
		where project_id = project_id
			and event_id = event_id
			and record = record
			and field_name = 'test_tpor_personnel';
		
		select value into test_tpor_batchsize
		from redcap_data
		where project_id = project_id
			and event_id = event_id
			and record = record
			and field_name = 'test_tpor_batchsize';
		
		select value into test_tpor_datetime
		from redcap_data
		where project_id = project_id
			and event_id = event_id
			and record = record
			and field_name = 'test_tpor_datetime';
			
		select value into tpor_lab
		from redcap_data
		where project_id = project_id
			and event_id = event_id
			and record = record
			and field_name = 'tpor_lab';
		-- ------------------------------------------------------------------
		-- Go through the specimen in the batch by looking for the 
		-- barcodes.
		-- ------------------------------------------------------------------
			
		open specimen;
		s_loop: LOOP
			fetch specimen into c_project_id, c_event_id, c_record, c_field_name, c_value, c_instance;		
			select substring(c_field_name, 17, 2) into pos;
							
			-- insert condition into the lab report
			insert into redcap_data (project_id, event_id, record, field_name, value)
			values (lab_report, report_max_event, c_value, 'test_tpor_barcode', c_value),
					(lab_report, report_max_event, c_value, 'test_tpor_personnel', test_tpor_personnel),
					(lab_report, report_max_event, c_value, 'test_tpor_datetime', test_tpor_datetime),
					(lab_report, report_max_event, c_value, 'test_tpor_id', pos),
					(lab_report, report_max_event, c_value, 'tpor_batch_pos', pos),
					(lab_report, report_max_event, c_value, 'tpor_lab', tpor_lab);
		END LOOP s_loop;
		
	end if;

END //

DELIMITER ;
