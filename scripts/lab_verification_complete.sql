DROP TRIGGER IF EXISTS `lab_verification_complete_update_trigger`;

DELIMITER $$
 
create trigger lab_verification_complete_update_trigger
after update
on redcap_data for each row
begin

	declare lab_report smallint(4);
	declare lab_receiving smallint(4);
	declare lab_extraction smallint(4);
	declare lab_results smallint(4);
    
	declare test_verify_batch_id varchar(100);
	declare test_verify_personnel varchar(100);
	declare authorised_to_verify varchar(100);
	declare test_verify_datetime datetime;
	declare test_verify_batchsize int(10);
	declare report_max_event smallint(4);
    
	declare c_project_id int(10);
	declare c_event_id int(10);
	declare c_record varchar(100);
	declare c_field_name varchar(100);
	declare c_value text;
	declare c_instance smallint(4);
	declare verify varchar(2);
	declare verified varchar(2);
    declare count_ int default 1;
    
	select 352 into lab_report;
	select 362 into lab_receiving;
	select 344 into lab_extraction;
	select 345 into lab_results;

	-- Stop calling this procedure with any other data
	if (new.field_name = 'verification_complete' and new.value = '2' and new.project_id = lab_results) then
        
		select value into test_verify_batchsize
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_verify_batchsize';
		
		-- test_verify_batch_id 
		select value into test_verify_batch_id 
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_verify_batch_id';
			
		-- test_verify_personnel
		select value into test_verify_personnel 
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_verify_personnel';
			
		-- test_verify_datetime
		select value into test_verify_datetime 
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_verify_datetime';
		
		select max(event_id) into report_max_event from redcap_data where project_id = lab_results;
				
		-- ------------------------------------------------------------------
		-- Go through the specimen in the batch by looking for the 
		-- barcodes.
		-- ------------------------------------------------------------------
        s2_loop:repeat
			
			-- Find the barcode
            select value
            into c_value
			from redcap_data
			where project_id = new.project_id
				and event_id = new.event_id
				and record = new.record
				and field_name = concat('test_det_barcode_', count_);
			
			select value into verify
			from redcap_data
			where project_id = c_project_id
				and event_id = c_event_id
				and record = c_record
				and field_name = concat('test_verify_result_', count_);
			
			select value into verified			
			from redcap_data
			where project_id = c_project_id
				and event_id = c_event_id
				and record = c_record
				and field_name = concat('covid_rna_results', count_);
			
			-- Update the staging area
			update redcap_ddp_specimen 
			set results_verified_by = test_verify_personnel,
			    results_verified_date = test_verify_datetime, 
			    test_verify_result = verify,
				COVID_RNA_RESULTS = verified,
				dhis2_synched = false
			where specimen_barcode = c_value;
            
			set count_ = count_ + 1;
        until count_ > test_verify_batchsize
        end repeat;
        		
	end if;
end; $$

DELIMITER ;

DROP TRIGGER IF EXISTS `lab_verification_complete_insert_trigger`;

DELIMITER $$
 
create trigger lab_verification_complete_insert_trigger
after update
on redcap_data for each row
begin

	declare lab_report smallint(4);
	declare lab_receiving smallint(4);
	declare lab_extraction smallint(4);
	declare lab_results smallint(4);
    
	declare test_verify_batch_id varchar(100);
	declare test_verify_personnel varchar(100);
	declare authorised_to_verify varchar(100);
	declare test_verify_datetime datetime;
	declare test_verify_batchsize int(10);
	declare report_max_event smallint(4);
    
	declare c_project_id int(10);
	declare c_event_id int(10);
	declare c_record varchar(100);
	declare c_field_name varchar(100);
	declare c_value text;
	declare c_instance smallint(4);
	declare verify varchar(2);
	declare verified varchar(2);
    declare count_ int default 1;
    
	select 352 into lab_report;
	select 362 into lab_receiving;
	select 344 into lab_extraction;
	select 345 into lab_results;

	-- Stop calling this procedure with any other data
	if (new.field_name = 'verification_complete' and new.value = '2' and new.project_id = lab_results) then
        
		select value into test_verify_batchsize
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_verify_batchsize';
		
		-- test_verify_batch_id 
		select value into test_verify_batch_id 
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_verify_batch_id';
			
		-- test_verify_personnel
		select value into test_verify_personnel 
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_verify_personnel';
			
		-- test_verify_datetime
		select value into test_verify_datetime 
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_verify_datetime';
		
		select max(event_id) into report_max_event from redcap_data where project_id = lab_results;
				
		-- ------------------------------------------------------------------
		-- Go through the specimen in the batch by looking for the 
		-- barcodes.
		-- ------------------------------------------------------------------
        s2_loop:repeat
			
			-- Find the barcode
            select value
            into c_value
			from redcap_data
			where project_id = new.project_id
				and event_id = new.event_id
				and record = new.record
				and field_name = concat('test_det_barcode_', count_);
			
			select value into verify
			from redcap_data
			where project_id = c_project_id
				and event_id = c_event_id
				and record = c_record
				and field_name = concat('test_verify_result_', count_);
			
			select value into verified			
			from redcap_data
			where project_id = c_project_id
				and event_id = c_event_id
				and record = c_record
				and field_name = concat('covid_rna_results', count_);
			
			-- Update the staging area
			update redcap_ddp_specimen 
			set results_verified_by = test_verify_personnel,
			    results_verified_date = test_verify_datetime, 
			    test_verify_result = verify,
				covid_rna_results = verified,
				dhis2_synched = false
			where specimen_barcode = c_value;
            
			set count_ = count_ + 1;
        until count_ > test_verify_batchsize
        end repeat;
        		
	end if;
end; $$

DELIMITER ;
