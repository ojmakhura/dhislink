DROP TRIGGER IF EXISTS `lab_reception_complete_condition_update_trigger`;

DELIMITER $$
 
create trigger lab_reception_complete_condition_update_trigger
after update
on redcap_data for each row
begin

	declare lab_receiving smallint(4);
	declare batchsize smallint(4);
	declare c_value text;
	declare count_ int default 1;	
	select 362 into lab_receiving;
	
	if (new.field_name = 'sarscov2_lab_reception_condition_complete' and new.value = '2' and new.project_id = lab_receiving) then
	
		select value into batchsize
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and redord = new.record
			and field_name = 'lab_rec_batchsize';
		
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
				and field_name = concat('lab_rec_barcode_', count_);
						
			-- Update the staging area
			update redcap_ddp_specimen 
			set dhis2_synched = false
			where specimen_barcode = c_value;
            
			set count_ = count_ + 1;
        until count_ > test_verify_batchsize
        end repeat;
		
	elseif (new.field_name = 'sarscov2_lab_reception_complete' and new.value = '2' and new.project_id = lab_receiving) then
		
		select value into batchsize
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and redord = new.record
			and field_name = 'lab_rec_batchsize';
		
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
				and field_name = concat('lab_rec_barcode_', count_);
						
			-- Update the staging area
			update redcap_ddp_specimen 
			set dhis2_synched = false
			where specimen_barcode = c_value;
            
			set count_ = count_ + 1;
        until count_ > test_verify_batchsize
        end repeat;
	end if;
end; $$

DELIMITER ;

DROP TRIGGER IF EXISTS `lab_reception_complete_condition_insert_trigger`;

DELIMITER $$
 
create trigger lab_reception_complete_condition_insert_trigger
after insert
on redcap_data for each row
begin

	declare lab_receiving smallint(4);
	declare batchsize smallint(4);
	declare c_value text;
	declare count_ int default 1;	
	select 362 into lab_receiving;
	
	if (new.field_name like 'lab_rec_barcode_%' and new.project_id = lab_receiving) then
		update redcap_ddp_specimen 
		set dhis2_synched = false
		where specimen_barcode = n;
	end if;
	
	if (new.field_name = 'sarscov2_lab_reception_condition_complete' and new.value = '2' and new.project_id = lab_receiving) then
	
		select value into batchsize
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and redord = new.record
			and field_name = 'lab_rec_batchsize';
		
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
				and field_name = concat('lab_rec_barcode_', count_);
						
			-- Update the staging area
			update redcap_ddp_specimen 
			set dhis2_synched = false
			where specimen_barcode = c_value;
            
			set count_ = count_ + 1;
        until count_ > test_verify_batchsize
        end repeat;
		
	elseif (new.field_name = 'sarscov2_lab_reception_complete' and new.value = '2' and new.project_id = lab_receiving) then
		
		select value into batchsize
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and redord = new.record
			and field_name = 'lab_rec_batchsize';
		
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
				and field_name = concat('lab_rec_barcode_', count_);
						
			-- Update the staging area
			update redcap_ddp_specimen 
			set dhis2_synched = false
			where specimen_barcode = c_value;
            
			set count_ = count_ + 1;
        until count_ > test_verify_batchsize
        end repeat;
	end if;
end; $$

DELIMITER ;

