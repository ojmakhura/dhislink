DROP PROCEDURE IF EXISTS labReceptionCondition;
DELIMITER //

-- ----------------------------------------------------------------------
-- Procedure for responding to a completion of the lab reception
-- condition form
-- ----------------------------------------------------------------------
CREATE PROCEDURE labReceptionCondition(
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
	declare c_project_id int(10);
	declare c_event_id int(10);
	declare c_record varchar(100);
	declare c_field_name varchar(100);
	declare c_value text;
	declare c_instance smallint(4);
	declare pos varchar(2);
	declare cond_field varchar(100);
	declare cond varchar(2);
	
	declare specimen cursor
	for
	select project_id, event_id, record, field_name, value, instance
	from redcap_data
	where project_id = project_id
		and event_id = event_id
		and record = record
		and field_name like 'lab_rec_barcode_%';
	
	-- Stop calling this procedure with any other data
	if (field_name = 'lab_reception_condition_complete' and i_value = '2' and project_id = 362) then
		select 352 into lab_report;
		select 362 into lab_receiving;
		select 344 into lab_extraction;
		select 345 into lab_results;

		select value into batch_size from redcap_data where record = new.record and field_name = 'lab_rec_batchsize';
		
		select max(event_id) into report_max_event from redcap_data where project_id = lab_report;
		
		-- ------------------------------------------------------------------
		-- Go through the specimen in the batch by looking for the 
		-- barcodes.
		-- ------------------------------------------------------------------
			
		open specimen;
		s_loop: LOOP
			fetch specimen into c_project_id, c_event_id, c_record, c_field_name, c_value, c_instance;
			
			select substring(c_field_name, 17, 2) into pos;
			select concat('specimen_cond_', pos) into cond_field;
			
			select value into cond
			from redcap_data
			where roject_id = c_project_id
				and event_id = c_event_id
				and record = c_record
				and field_name = cond_field;
			
			-- Update the staging area
			update redcap_ddp_specimen 
			set receiving_condition_code = cond,
				dhis2_synched = false
			where specimen_barcode = c_value;
					
			-- insert condition into the lab report
			insert into redcap_data (project_id, event_id, record, field_name, value)
			values 	(lab_report, report_max_event, c_value, 'receiving_condition_code', cond);
		END LOOP s_loop;
		
	end if;

END //

DELIMITER ;
